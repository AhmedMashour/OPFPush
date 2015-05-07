/*
 * Copyright 2012-2015 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.pushchat.ui.fragment.content;

import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.onepf.pushchat.R;
import org.onepf.pushchat.controller.NotificationController;
import org.onepf.pushchat.controller.StateController;
import org.onepf.pushchat.db.ContentDescriptor.MessagesContract;
import org.onepf.pushchat.db.DatabaseHelper;
import org.onepf.pushchat.model.Message;
import org.onepf.pushchat.model.PushState;
import org.onepf.pushchat.model.response.push.FailedPushResult;
import org.onepf.pushchat.model.response.push.PushMessageResponse;
import org.onepf.pushchat.retrofit.NetworkController;
import org.onepf.pushchat.ui.adapter.MessagesCursorAdapter;
import org.onepf.pushchat.ui.dialog.AlertDialogFragment;
import org.onepf.pushchat.utils.ContactsProvider;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.CLIPBOARD_SERVICE;
import static org.onepf.pushchat.db.ContentDescriptor.MessagesContract.MessageEntry.RECEIVED_TIME;
import static org.onepf.pushchat.utils.Constants.*;

/**
 * @author Roman Savin
 * @since 28.04.2015
 */
public class MessagesFragment extends BaseContentFragment {

    private MessagesCursorAdapter adapter;

    private EditText messageEditText;

    private UpdateStateReceiver receiver;

    @NonNull
    public static MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_messages, container, false);

        messageEditText = (EditText) view.findViewById(R.id.message_input);
        messageEditText.setOnEditorActionListener(onEditorActionListener());

        messageEditText.setEnabled(StateController.getState(getActivity()) == PushState.REGISTERED);

        showClearButton();
        initMessagesList(view);
        initLoaderManager();

        registerReceiver();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationController.getInstance().setNeedShowNotification(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationController.getInstance().setNeedShowNotification(true);
        hideKeyboard(getView());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceiver();
        hideClearButton();
        messageEditText = null;
    }

    private TextView.OnEditorActionListener onEditorActionListener() {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                final String messageText = textView.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return false;
                }

                final Context context = getActivity();
                if (ContactsProvider.getUuids(context).isEmpty()) {
                    final AlertDialogFragment dialogFragment = AlertDialogFragment
                            .newInstance(getString(R.string.empty_contact_list));
                    dialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
                } else {
                    showProgressBar();

                    NetworkController.getInstance().pushMessage(
                            context,
                            messageText,
                            pushMessageCallback(messageText)
                    );
                    textView.setText("");
                }
                return true;
            }
        };
    }

    private void registerReceiver() {
        if (receiver == null) {
            receiver = new UpdateStateReceiver();
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(REGISTERED_ACTION);
            intentFilter.addAction(UNREGISTERED_ACTION);
            getActivity().registerReceiver(receiver, intentFilter);
        }
    }

    private void unregisterReceiver() {
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void enableMessageEditText() {
        if (messageEditText != null) {
            messageEditText.setEnabled(true);
        }
    }

    private void disableMessageEditText() {
        if (messageEditText != null) {
            messageEditText.setEnabled(false);
        }
    }

    private void initMessagesList(@NonNull final View view) {
        final ListView messagesListView = (ListView) view.findViewById(R.id.messages_list);

        adapter = new MessagesCursorAdapter(getActivity());
        messagesListView.setAdapter(adapter);

        messagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //copy sender uuid.
                final TextView senderTextView = (TextView) view.findViewById(R.id.sender);
                final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText(
                        getString(R.string.sender_uuid_clipdata_label),
                        senderTextView.getText()
                ));

                Toast.makeText(getActivity(), R.string.sender_uuid_copy_toast, Toast.LENGTH_SHORT).show();

                return true;
            }
        });
    }

    private void initLoaderManager() {
        final LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity(), MessagesContract.TABLE_URI, null, null, null, RECEIVED_TIME + " DESC");
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                if (adapter != null && cursor != null) {
                    adapter.changeCursor(cursor);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                if (adapter != null) {
                    adapter.changeCursor(null);
                }
            }
        });
    }

    private Callback<PushMessageResponse> pushMessageCallback(@NonNull final String messageText) {
        return new Callback<PushMessageResponse>() {

            @Override
            public void success(@NonNull final PushMessageResponse pushMessageResponse,
                                @NonNull final Response response) {
                hideProgressBar();
                DatabaseHelper.getInstance(getActivity()).addMessage(new Message(
                        getString(R.string.sender_you),
                        messageText,
                        System.currentTimeMillis()
                ));

                final FailedPushResult[] failedPushResults = pushMessageResponse.failed;
                if (failedPushResults != null && failedPushResults.length > 0) {
                    final StringBuilder errorBuilder = new StringBuilder("Can't send message to the following uuids:")
                            .append(LINE_SEPARATOR);
                    for (FailedPushResult failedPushResult : failedPushResults) {
                        errorBuilder.append(LINE_SEPARATOR)
                                .append("UUID : \"")
                                .append(failedPushResult.pushReceiver.uuid)
                                .append("\" Reason : ")
                                .append(failedPushResult.errorMessage)
                                .append(LINE_SEPARATOR);
                    }

                    final AlertDialogFragment dialogFragment = AlertDialogFragment.newInstance(errorBuilder.toString());
                    dialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
                }
            }

            @Override
            public void failure(@NonNull final RetrofitError error) {
                hideProgressBar();
                final AlertDialogFragment dialogFragment = AlertDialogFragment.newInstance(error.getMessage());
                dialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
            }
        };
    }

    public class UpdateStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case REGISTERED_ACTION:
                    enableMessageEditText();
                    break;
                case UNREGISTERED_ACTION:
                    disableMessageEditText();
                    break;
            }
        }
    }
}
