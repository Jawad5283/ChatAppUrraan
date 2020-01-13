package com.destructors.chatappqb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.se.omapi.Session;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.destructors.chatappqb.Adapter.ChatDialogsAdapter;
import com.destructors.chatappqb.Common.Common;
import com.destructors.chatappqb.Holder.QBChateDialogHolder;
import com.destructors.chatappqb.Holder.QBUnreadMessageHolder;
import com.destructors.chatappqb.Holder.QBUserHolder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatDialogsActivity extends AppCompatActivity
        implements QBSystemMessageListener, QBChatDialogMessageListener {

    FloatingActionButton floatingActionButton;
    ListView listChatDialogs;


    @Override
    protected void onResume() {
        super.onResume();
        loadChateDialogs();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_dialog_context_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId())
        {
            case R.id.context_delete_dialog:
                deleteDialog(info.position);
                break;
                default:
                    break;
        }
        return true;
    }

    private void deleteDialog(int index) {
        final QBChatDialog chatDialog = (QBChatDialog) listChatDialogs.getAdapter().getItem(index);
        QBRestChatService.deleteDialog(chatDialog.getDialogId(),false)
                .performAsync(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        QBChateDialogHolder.getInstance().removeDialog(chatDialog.getDialogId());
                        ChatDialogsAdapter adapter = new ChatDialogsAdapter(getBaseContext(),QBChateDialogHolder.getInstance().getAllChatDialogs());
                        listChatDialogs.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(ChatDialogsActivity.this, "Chat Dalog deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ChatDialogsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_dialogs);

        //toolbar
        Toolbar toolbar = findViewById(R.id.chat_dialog_toolbar);
        toolbar.setTitle("Chat App");
        setSupportActionBar(toolbar);

        createSessionForChat();

        listChatDialogs = findViewById(R.id.list_chat_dialogs);
        registerForContextMenu(listChatDialogs);
        listChatDialogs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog qbChatDialog = (QBChatDialog)listChatDialogs.getAdapter().getItem(position);
                Intent intent = new Intent(ChatDialogsActivity.this,ChatMessageActivity.class);
                intent.putExtra(Common.DIALOGE_EXTRA,qbChatDialog);
                startActivity(intent);
            }
        });

        loadChateDialogs();

        floatingActionButton = findViewById(R.id.add_users);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDialogsActivity.this,LIstUsersActivity.class);
                startActivity(intent);
            }
        });


    }

    private void loadChateDialogs() {
        QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();
        requestGetBuilder.setLimit(100);

        QBRestChatService.getChatDialogs(null,requestGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {

                //put all dialogs to cache
                QBChateDialogHolder.getInstance().putDialogs(qbChatDialogs);

                //unread Settings
                Set<String> setIds = new HashSet<>();
                for (QBChatDialog chatDialog : qbChatDialogs)
                    setIds.add(chatDialog.getDialogId());

                //Get message unread
                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMessageHolder.getInstance().getBundle())
                        .performAsync(new QBEntityCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer, Bundle bundle) {
                                //save to cashe
                                QBUnreadMessageHolder.getInstance().setBundle(bundle);

                                //Refresh list dialogs
                                ChatDialogsAdapter adapter = new ChatDialogsAdapter(getBaseContext(),QBChateDialogHolder.getInstance().getAllChatDialogs());
                                listChatDialogs.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Toast.makeText(ChatDialogsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ChatDialogsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void createSessionForChat() {
        final ProgressDialog dialog = new ProgressDialog(ChatDialogsActivity.this);
        dialog.setMessage("please Wait....");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        String user,password;
        user = getIntent().getStringExtra("user");
        password = getIntent().getStringExtra("password");

        //Load all users and saved to cache
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUserHolder.getInstance().putUsers(qbUsers);
                Toast.makeText(ChatDialogsActivity.this, "All users loaded successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ChatDialogsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        final QBUser qbUser = new QBUser(user,password);

        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                Toast.makeText(ChatDialogsActivity.this, "session created", Toast.LENGTH_SHORT).show();
                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }

                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        dialog.dismiss();

                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ChatDialogsActivity.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatDialogsActivity.this);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ChatDialogsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ChatDialogsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        Toast.makeText(this, "QBSystemMessageListner", Toast.LENGTH_SHORT).show();

        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                //put to cache
                QBChateDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource = QBChateDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogsAdapter adapter = new ChatDialogsAdapter(getBaseContext(),adapterSource);
                listChatDialogs.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ChatDialogsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        //qbchatmessageListner
        loadChateDialogs();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_dialog_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.chat_dialog_menu_user:
                showUserProfile();
                break;
                default:
                    break;
        }
        return true;
    }

    private void showUserProfile() {
        Intent intent = new Intent(ChatDialogsActivity.this,UserProfileActivity.class);
        startActivity(intent);
    }
}
