package com.destructors.chatappqb.Holder;

import android.util.SparseArray;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class QBUserHolder {

    private static QBUserHolder instance;
    private SparseArray<QBUser> qbUserSparseArray;

    public static synchronized QBUserHolder getInstance(){
        if (instance==null)
            instance = new QBUserHolder();
        return instance;
    }

    private QBUserHolder(){
        qbUserSparseArray = new SparseArray<>();
    }

    public void putUsers(List<QBUser> users)
    {
        for (QBUser user : users)
            PutUser(user);
    }

    private void PutUser(QBUser user) {
        qbUserSparseArray.put(user.getId(),user);

    }

    public QBUser getUserById(int id)
    {
        return qbUserSparseArray.get(id);
    }

    public List<QBUser> getUsersByIds(List<Integer> ids)
    {
        List<QBUser> qbUser = new ArrayList<>();
        for (Integer id:ids)
        {
            QBUser user = getUserById(id);
            if (user!=null)
                qbUser.add(user);
        }
        return qbUser;
    }

    public ArrayList<QBUser> getAllUsers() {
        ArrayList<QBUser> result = new ArrayList<>();
        for (int i=0;i<qbUserSparseArray.size();i++)
            result.add(qbUserSparseArray.valueAt(i));
        return result;
    }
}















