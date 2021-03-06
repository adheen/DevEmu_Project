package org.devemu.sql.dao.impl.account;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.devemu.program.Main;
import org.devemu.sql.DAOPreloader;
import org.devemu.sql.Database;
import org.devemu.sql.GetDatabase;
import org.devemu.sql.entity.Account;
import org.devemu.sql.entity.manager.AccountManager;
import org.devemu.utils.config.ConfigEnum;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class AccountDAOPreloader implements DAOPreloader<Account> {

    private List<String> preload;
    private GetDatabase getDb = new GetDatabase() {
        @Override
        public Database getDatabase() {
            return Database.getDatabase((String) Main.getConfigValue(ConfigEnum.DATABASE_NAME));
        }
    };
    private Database database;

    public AccountDAOPreloader() {
        
    }
    
    @Override
    public int preload() {
        if (preload == null) {
            preload = new ArrayList<String>(512);
        }
        
        if (!preload.isEmpty()) {
            return preload.size();
        }
        
        if (database == null) {
            database = getDb.getDatabase();
        }
        
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = database.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM accounts");
            while (resultSet.next()) {
                String s = Joiner.on(";").join(new Object[]{
                        Long.toString(resultSet.getLong("id")), 
                        resultSet.getString("name"), 
                        resultSet.getString("password"), 
                        resultSet.getString("pseudo"),
                        Integer.toString(resultSet.getInt("rank")),
                        resultSet.getString("question"),
                        resultSet.getString("answer"),
                        Long.toString(resultSet.getLong("unbanDate")),
                        Long.toString(resultSet.getLong("suscribeEnd")),
                        Byte.toString(resultSet.getByte("community")),
                        resultSet.getString("characters"),
                        Byte.toString(resultSet.getByte("maxCharacters")),
                        Long.toString(resultSet.getLong("points"))});
                preload.add(s);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            Database.close(resultSet, statement, connection);
        }
        
        return preload.size();
    }

    @Override
    public Collection<Account> load() {
        if (preload == null) {
            return new ArrayList<Account>(0);
        }
        List<Account> accounts = new ArrayList<Account>(preload.size());
        for (String s : preload) {
            accounts.add(AccountManager.create(s.split(";")));
        }
        preload.clear();
        return accounts;
    }

    @Override
    public void unload() {
        if (preload == null || preload.isEmpty()) {
            return;
        }
        preload.clear();
    }

    @Override
    public void setGetDb(GetDatabase getDb) {
        this.getDb = getDb;
    }
    
}
