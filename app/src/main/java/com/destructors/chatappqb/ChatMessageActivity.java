package com.destructors.chatappqb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.destructors.chatappqb.Adapter.ChatMessagesAdapter;
import com.destructors.chatappqb.Common.Common;
import com.destructors.chatappqb.Holder.QBChatMessagesHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBChatDialogTypingListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.squareup.picasso.Picasso;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener{

    QBChatDialog qbChatDialog;
    ListView listChatMessages;
    ImageButton submitButton;
    EditText edtContent;
    Toolbar toolbar;

    //Update online users
    ImageView img_online_count;
    TextView txt_online_count;

    static final int SELECT_PICTURE = 7171;

    //variables for edit/delete messages
    int contextMenuIndexedClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;

    ChatMessagesAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP)
            getMenuInflater().inflate(R.menu.chat_message_group_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.chat_group_edit_name:
                editNameGroup();
                break;
            case R.id.chat_group_add_user:
                addUser();
                break;
            case R.id.chat_group_remove_user:
                removeUser();
                break;
                default:
                    break;
        }
        return true;
    }

    private void removeUser() {
        Intent intent = new Intent(ChatMessageActivity.this,LIstUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA,qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE,Common.UPDATE_REMOVE_MODE);
        startActivity(intent);
    }

    private void addUser() {
        Intent intent = new Intent(ChatMessageActivity.this,LIstUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA,qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE,Common.UPDATE_ADD_MODE);
        startActivity(intent);
    }

    private void editNameGroup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_group_layout,null);

        final EditText newName = view.findViewById(R.id.edt_new_group_name);

        new AlertDialog.Builder(ChatMessageActivity.this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        qbChatDialog.setName(newName.getText().toString());  //setNew name for dialog

                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(ChatMessageActivity.this, "Group name Updated", Toast.LENGTH_SHORT).show();
                                        toolbar.setTitle(qbChatDialog.getName());
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(ChatMessageActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
                .create().show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_message_context_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        //get index context menu click
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        contextMenuIndexedClicked = info.position;

        switch (item.getItemId())
        {
            case R.id.chat_message_Delete_message:
                deleteMessage();
                break;
            case R.id.chat_message_update_message:
                updateMessage();
                break;
                default:
                    break;
        }
        return true;
    }

    private void deleteMessage() {

        final ProgressDialog deleteDialog = new ProgressDialog(ChatMessageActivity.this);
        deleteDialog.setMessage("please wait...");
        deleteDialog.show();

        editMessage = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexedClicked);
        QBRestChatService.deleteMessage(editMessage.getId(),false)
                .performAsync(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        Toast.makeText(ChatMessageActivity.this, "Message deleted successfully", Toast.LENGTH_SHORT).show();
                        retriveMessages();
                        deleteDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ChatMessageActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        deleteDialog.dismiss();
                    }
                });
    }

    private void updateMessage() {
        //set message for editText
        editMessage = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexedClicked);
        edtContent.setText(editMessage.getBody());
        isEditMode = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        initViews();
        initChatDialogs();
        retriveMessages();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtContent.getText().toString().isEmpty()) {
                    if (!isEditMode) {
                        QBChatMessage chatMessage = new QBChatMessage();
                        chatMessage.setBody(edtContent.getText().toString());
                        chatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
                        chatMessage.setSaveToHistory(true);

                        try {
                            qbChatDialog.sendMessage(chatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        //fix private chat dont show message
                        if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                            //save messages to cache
                            QBChatMessagesHolder.getInstance().putMessage(qbChatDialog.getDialogId(), chatMessage);

                            ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(chatMessage.getDialogId());
                            adapter = new ChatMessagesAdapter(getBaseContext(), messages);
                            listChatMessages.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }

                        edtContent.setText("");
                        edtContent.setFocusable(true);
                    } else {
                        final ProgressDialog updateDialog = new ProgressDialog(ChatMessageActivity.this);
                        updateDialog.setMessage("please wait...");
                        updateDialog.show();


                        QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
                        messageUpdateBuilder.updateText(edtContent.getText().toString()).markDelivered().markRead();

                        QBRestChatService.updateMessage(editMessage.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder)
                                .performAsync(new QBEntityCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid, Bundle bundle) {
                                        Toast.makeText(ChatMessageActivity.this, "Message Updated Successfully", Toast.LENGTH_SHORT).show();
                                        //refress data
                                        retriveMessages();
                                        isEditMode = false; //reset variables
                                        updateDialog.dismiss();

                                        edtContent.setText("");
                                        edtContent.setFocusable(true);
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(ChatMessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }else
                {
                    Toast.makeText(ChatMessageActivity.this, "You can't send Empty Message!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void retriveMessages() {
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(500);  // get limit 500 sms

        if (qbChatDialog!=null)
        {
            QBRestChatService.getDialogMessages(qbChatDialog,messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    // put messages to cache
                    QBChatMessagesHolder.getInstance().putMessages(qbChatDialog.getDialogId(),qbChatMessages);
                    adapter = new ChatMessagesAdapter(getBaseContext(),qbChatMessages);
                    listChatMessages.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {
                    Toast.makeText(ChatMessageActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initChatDialogs() {
        qbChatDialog = (QBChatDialog)getIntent().getSerializableExtra(Common.DIALOGE_EXTRA);

//        if (qbChatDialog.getPhoto() != null && !qbChatDialog.getPhoto().equals("null"))
//        {
//            QBContent.getFile(Integer.parseInt(qbChatDialog.getPhoto()))
//                    .performAsync(new QBEntityCallback<QBFile>() {
//                        @Override
//                        public void onSuccess(QBFile qbFile, Bundle bundle) {
//                            String fileUrl = qbFile.getPublicUrl();
//                            Picasso.with(getBaseContext())
//                                    .load(fileUrl)
//                                    .resize(50,50)
//                                    .centerCrop()
//                                    .into(dialog_avatar);
//
//                            Toast.makeText(ChatMessageActivity.this, "Image set to Dialog avatar", Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onError(QBResponseException e) {
//                            Toast.makeText(ChatMessageActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }

        qbChatDialog.initForChat(QBChatService.getInstance());

        //register listner for incomming messages
        QBIncomingMessagesManager incomingMessages = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessages.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
                Toast.makeText(ChatMessageActivity.this, "incomming message successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
                Toast.makeText(ChatMessageActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Add typing Listner
        RegisterTypingForChatDialog(qbChatDialog);

        //add join group to enable group chat
        if (qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP)
        {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);

            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {
                    Toast.makeText(ChatMessageActivity.this, "discussion history success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(QBResponseException e) {
                    Toast.makeText(ChatMessageActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

        final QBChatDialogParticipantListener participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {
                if (dialogId == qbChatDialog.getDialogId())
                {
                    QBRestChatService.getChatDialogById(dialogId)
                            .performAsync(new QBEntityCallback<QBChatDialog>() {
                                @Override
                                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                    //Get online user
                                    Toast.makeText(ChatMessageActivity.this, "getting online users", Toast.LENGTH_SHORT).show();
                                    try {
                                        Collection<Integer> onlineList = qbChatDialog.getOnlineUsers();
                                        TextDrawable.IBuilder builder = TextDrawable.builder()
                                                .beginConfig()
                                                .withBorder(4)
                                                .endConfig()
                                                .round();
                                        TextDrawable online = builder.build("", Color.GREEN);
                                        img_online_count.setImageDrawable(online);
                                        txt_online_count.setText(String.format("%d/%d online",onlineList.size(),qbChatDialog.getOccupants().size()));

                                    } catch (XMPPException e) {
                                        e.printStackTrace();
                                    } catch (SmackException.NotConnectedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    Toast.makeText(ChatMessageActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }
        };

        qbChatDialog.addParticipantListener(participantListener);

        qbChatDialog.addMessageListener(this);

        //setTitle for toolbar
        toolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(toolbar);

    }

    private void RegisterTypingForChatDialog(QBChatDialog qbChatDialog) {
        QBChatDialogTypingListener typingListener = new QBChatDialogTypingListener() {
            @Override
            public void processUserIsTyping(String dialogId, Integer integer) {
                //code late
                Toast.makeText(ChatMessageActivity.this, "Typing...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void processUserStopTyping(String dialogId, Integer integer) {

            }
        };
    }

    private void initViews() {
        listChatMessages = findViewById(R.id.list_of_message);
        submitButton = findViewById(R.id.send_button);
        edtContent = findViewById(R.id.edt_content);
        img_online_count = findViewById(R.id.img_online_count);
        txt_online_count = findViewById(R.id.txt_online_count);


        //add contextMenu
        registerForContextMenu(listChatMessages);

        //add toolbar
        toolbar =  findViewById(R.id.chat_message_toolbar);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                final ProgressDialog mDialog = new ProgressDialog(ChatMessageActivity.this);
                mDialog.setMessage("Please wait...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                //convert uri to file
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    File file = new File(Environment.getExternalStorageDirectory() + "/image.png");
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(bos.toByteArray());
                    fileOutputStream.flush();
                    fileOutputStream.close();

                    int imageSizeKb = (int) (file.length() / 1024);
                    if (imageSizeKb >= 1024 * 100) {
                        Toast.makeText(this, "image is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //upload file
                    QBContent.uploadFileTask(file, true, null)
                            .performAsync(new QBEntityCallback<QBFile>() {
                                @Override
                                public void onSuccess(QBFile qbFile, Bundle bundle) {
                                    Toast.makeText(ChatMessageActivity.this, "file uploaded successfully", Toast.LENGTH_SHORT).show();
                                    qbChatDialog.setPhoto(qbFile.getId().toString());

                                    //update chat dialog
                                    QBRequestUpdateBuilder requestUpdateBuilder = new QBRequestUpdateBuilder();
                                    QBRestChatService.updateGroupChatDialog(qbChatDialog, requestUpdateBuilder)
                                            .performAsync(new QBEntityCallback<QBChatDialog>() {
                                                @Override
                                                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                                    mDialog.dismiss();
                                                    Toast.makeText(ChatMessageActivity.this, "dialoge Image Updated successfully", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onError(QBResponseException e) {
                                                    Toast.makeText(ChatMessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    Toast.makeText(ChatMessageActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        Toast.makeText(this, "message success", Toast.LENGTH_SHORT).show();

        //save messages to cache
        QBChatMessagesHolder.getInstance().putMessage(qbChatMessage.getDialogId(),qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatMessage.getDialogId());
        adapter = new ChatMessagesAdapter(getBaseContext(),messages);
        listChatMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
