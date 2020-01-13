package com.destructors.chatappqb;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.destructors.chatappqb.Adapter.ListUsersAdapter;
import com.destructors.chatappqb.Common.Common;
import com.destructors.chatappqb.Holder.QBUserHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.List;

public class LIstUsersActivity extends AppCompatActivity {

    ListView lstUsers;
    Button btnCreateChat;

    //get intent for group chat
    String mode = "";
    QBChatDialog qbChatDialog;
    List<QBUser> userAdd = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);

        mode = getIntent().getStringExtra(Common.UPDATE_MODE);
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.UPDATE_DIALOG_EXTRA);

        lstUsers = findViewById(R.id.list_users);
        //RetrieveAllUsers();

        lstUsers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btnCreateChat = findViewById(R.id.btn_create_chat);
        btnCreateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mode == null) {


                    int countChoice = lstUsers.getCount();
                    if (lstUsers.getCheckedItemPositions().size() == 1)
                        createPrivateChat(lstUsers.getCheckedItemPositions());
                    else if (lstUsers.getCheckedItemPositions().size() > 1)
                        createGroupChat(lstUsers.getCheckedItemPositions());
                    else
                        Toast.makeText(LIstUsersActivity.this, "Please select a friend to chat", Toast.LENGTH_SHORT).show();
                }
                else if (mode.equals(Common.UPDATE_ADD_MODE) &&qbChatDialog !=null)
                {
                    if (userAdd.size()>0)
                    {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        int cntChoice = lstUsers.getCount();
                        SparseBooleanArray checkItemPositions = lstUsers.getCheckedItemPositions();
                        for (int i=0;i<cntChoice;i++)
                        {
                            if (checkItemPositions.get(i))
                            {
                                QBUser user = (QBUser) lstUsers.getItemAtPosition(i);
                                requestBuilder.addUsers(user);
                            }
                        }
                        //call service
                        QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(LIstUsersActivity.this, "User added Successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(LIstUsersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
                else if (mode.equals(Common.UPDATE_REMOVE_MODE) && qbChatDialog!=null)
                {
                    if (userAdd.size()>0)
                    {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        int cntChoice = lstUsers.getCount();
                        SparseBooleanArray checkItemPositions = lstUsers.getCheckedItemPositions();
                        for (int i=0;i<cntChoice;i++)
                        {
                            if (checkItemPositions.get(i))
                            {
                                QBUser user = (QBUser) lstUsers.getItemAtPosition(i);
                                requestBuilder.removeUsers(user);
                            }
                        }
                        //call service
                        QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(LIstUsersActivity.this, "User removed Successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(LIstUsersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }
        });

        if (mode==null && qbChatDialog==null)
            RetrieveAllUsers();
        else if (mode.equals(Common.UPDATE_ADD_MODE))
            LoadListAvailibleUser();
        else if (mode.equals(Common.UPDATE_REMOVE_MODE))
            LoadListUserInGroup();

    }

    private void LoadListUserInGroup() {
        btnCreateChat.setText("Remove User");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        List<Integer> occupantId = qbChatDialog.getOccupants();
                        List<QBUser> listUserAlreadyInGroup = QBUserHolder.getInstance().getUsersByIds(occupantId);
                        ArrayList<QBUser> users = new ArrayList<>();
                        users.addAll(listUserAlreadyInGroup);


                        ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(),users);
                        lstUsers.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        userAdd = users;
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
    }

    private void LoadListAvailibleUser() {
        btnCreateChat.setText("Add user");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        ArrayList<QBUser> listUsers = QBUserHolder.getInstance().getAllUsers();
                        List<Integer> occupantsIds = qbChatDialog.getOccupants();
                        List<QBUser> listUserAlreadyInChatGroup = QBUserHolder.getInstance().getUsersByIds(occupantsIds);

                        //remove all users who are already in chat group
                        for (QBUser user : listUserAlreadyInChatGroup)
                            listUsers.remove(user);
                        if (listUsers.size()>0)
                        {
                            ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(),listUsers);
                            lstUsers.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            userAdd = listUsers;

                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog dialog = new ProgressDialog(LIstUsersActivity.this);
        dialog.setMessage("Please wait...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        int countChoice = lstUsers.getCount();
        ArrayList<Integer> occupantIdsList = new ArrayList<>();
        for (int i=0;i<countChoice;i++)
        {
            if (checkedItemPositions.get(i))
            {
                QBUser user = (QBUser)lstUsers.getItemAtPosition(i);
                occupantIdsList.add(user.getId());
            }
        }
        //create chat dialog
        QBChatDialog chatDialog = new QBChatDialog();
        chatDialog.setName(Common.createChatDialogName(occupantIdsList));
        chatDialog.setType(QBDialogType.GROUP);
        chatDialog.setOccupantsIds(occupantIdsList);

        QBRestChatService.createChatDialog(chatDialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                Toast.makeText(LIstUsersActivity.this, "chat dialog created succcessrully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                //send system message to recipient id user
                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for (int i=0;i<qbChatDialog.getOccupants().size();i++)
                {
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));
                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }


                }



                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(LIstUsersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPrivateChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog dialog = new ProgressDialog(LIstUsersActivity.this);
        dialog.setMessage("Please wait...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        int countChoice = lstUsers.getCount();
        for (int i=0;i<countChoice;i++)
        {
            if (checkedItemPositions.get(i))
            {
                final QBUser user = (QBUser)lstUsers.getItemAtPosition(i);
                QBChatDialog chatDialog = DialogUtils.buildPrivateDialog(user.getId());

                QBRestChatService.createChatDialog(chatDialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        dialog.dismiss();
                        Toast.makeText(LIstUsersActivity.this, "private dialog created successfully", Toast.LENGTH_SHORT).show();

                        //send system message to recipient id user
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage = new QBChatMessage();
                        qbChatMessage.setRecipientId(user.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {
                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(LIstUsersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void RetrieveAllUsers() {
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {

                //add to cashe
                QBUserHolder.getInstance().putUsers(qbUsers);

                ArrayList<QBUser> qbUsersWithoutCurrent = new ArrayList<QBUser>();
                for (QBUser user : qbUsers)
                {
                    if (!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin()))
                        qbUsersWithoutCurrent.add(user);
                }
                ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(),qbUsersWithoutCurrent);
                lstUsers.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(LIstUsersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
