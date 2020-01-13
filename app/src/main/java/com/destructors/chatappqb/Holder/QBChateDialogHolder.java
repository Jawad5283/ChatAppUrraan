package com.destructors.chatappqb.Holder;

import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QBChateDialogHolder {

    private static QBChateDialogHolder instance;
    private HashMap<String, QBChatDialog> qbChatDialogHashMap;

    public static synchronized QBChateDialogHolder getInstance()
    {
        QBChateDialogHolder qbChateDialogHolder;
        synchronized (QBChateDialogHolder.class)
        {
            if (instance==null)
            {
                instance = new QBChateDialogHolder();
            }
            qbChateDialogHolder = instance;
            return qbChateDialogHolder;
        }
    }

    public QBChateDialogHolder() {
        this.qbChatDialogHashMap = new HashMap<>();
    }

    public void putDialogs(List<QBChatDialog> dialogs)
    {
        for (QBChatDialog qbChatDialog : dialogs)
            putDialog(qbChatDialog);
    }

    public void putDialog(QBChatDialog qbChatDialog) {
        this.qbChatDialogHashMap.put(qbChatDialog.getDialogId(),qbChatDialog);
    }

    public QBChatDialog getChatByDialogId(String dialogId)
    {
        return qbChatDialogHashMap.get(dialogId);
    }

    public List<QBChatDialog> getChatDialogsByIds(List<String> dialogsIds)
    {
        List<QBChatDialog> chatDialogs = new ArrayList<>();
        for (String id : dialogsIds)
        {
            QBChatDialog chatDialog = getChatByDialogId(id);
            if (chatDialog!=null)
                chatDialogs.add(chatDialog);
        }
        return chatDialogs;
    }

    public ArrayList<QBChatDialog> getAllChatDialogs(){
        ArrayList<QBChatDialog> qbChat = new ArrayList<>();
        for (String key : qbChatDialogHashMap.keySet())
            qbChat.add(qbChatDialogHashMap.get(key));
        return qbChat;
    }

    public void removeDialog(String id)
    {
        qbChatDialogHashMap.remove(id);
    }
}
