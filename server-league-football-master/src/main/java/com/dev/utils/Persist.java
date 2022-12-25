
package com.dev.utils;
import com.dev.objects.Group;
import com.dev.objects.Match;
import com.dev.objects.UserObject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class Persist {

    private Connection connection;

    private final SessionFactory sessionFactory;

    @Autowired
    public Persist(SessionFactory sf) {
        this.sessionFactory = sf;
    }


    @PostConstruct
    public void createConnectionToDatabase() {

        try {
            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/football_project", "root", "1234");
            System.out.println("Successfully connected to DB");
            if (checkIfTableEmpty()) {
                initGroups();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Match> getMatchesFinished() {
        return  sessionFactory.openSession().createQuery("from Match where isLive =: false")
                .setParameter("false", false).list();
    }

    public void finishMatch(Match match) {
        match.getTeam1();
    }



    public void deleteUser(String usernameToDelete) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        session.createQuery("delete from UserObject where username=:username")
                .setParameter("username", usernameToDelete).executeUpdate();
        tx.commit();
        session.close();
    }

    public void finishMatch(String team1) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.createQuery("delete from Match where team1=:team1").setParameter("team1", team1).executeUpdate();
        tx.commit();
        session.close();

    }



    public void updateTeam1Goals(String team , int updateGoals) {

        int matchId = (Integer) sessionFactory.openSession().createQuery("select id from Match where team1 = : team")
                .setParameter("team", team).list().get(0);
        System.out.println();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        tx = session.beginTransaction();
        Match match = session.get(Match.class, matchId);
        match.setTeam1Goals(updateGoals);
        session.update(match);
        tx.commit();
    }
    public void updateTeam2Goals(String team, int updateGoals) {
        List<Integer> idList =  sessionFactory.openSession().createQuery("select id from Match where team2 = : team")
                .setParameter("team", team).list();
        int matchId = idList.get(0);
        System.out.println();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        tx = session.beginTransaction();
        Match match = session.get(Match.class, matchId);
        match.setTeam2Goals(updateGoals);
        session.update(match);
        tx.commit();
    }

    //Transaction test
    public void updateUserName(String username , String token ) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        UserObject userObject = (UserObject) session.get(UserObject.class, username);
        userObject.setToken(token);
        session.update(userObject);
        tx.commit();

    }

    public boolean checkIfTeamIsPlaying(String team1, String team2){
        boolean isPlaying= false;
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(
                    "SELECT id " +
                            "FROM live_games " +
                            "WHERE team1 = ? or team2 = ?");
            preparedStatement.setString(1, team1);
            preparedStatement.setString(2, team2);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
               isPlaying = false;
            } else {
                isPlaying = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isPlaying;
    }

    private boolean checkIfTableEmpty() {
        boolean empty = false;
        List<Group> groups = sessionFactory.openSession()
                .createQuery("FROM Group").list();
        if (groups.isEmpty()) {
            empty = true;
        }
        return empty;
    }



    public List<String> getAllGroupsName () {
        return (List<String>) sessionFactory.openSession().createQuery("SELECT name FROM Group").list();
    }
        private void initGroups() {

            List<Group> club = new ArrayList<>();
            club.add(new Group("Maccabi-Ashdod"));
            club.add(new Group("Hapoel-Afula"));
            club.add(new Group("Shaaraiim"));
            club.add(new Group("Bnai-Reina"));
            club.add(new Group("Kiryat-Gat"));
            club.add(new Group("Arayot-Rahat"));
            club.add(new Group("Bnai-Ashkelon"));
            club.add(new Group("Netivot"));
            club.add(new Group("Leviot-Yeruham"));
            club.add(new Group("Totahi-Ramle"));
            club.add(new Group("Hapoel-Natanya"));
            club.add(new Group("Milan"));

            for (Group group : club) {
                sessionFactory.openSession().save(group);
            }


}

    public List<Group> getGroups() {
        return sessionFactory.openSession().createQuery("from Group ").list();
    }


    public List<UserObject> getAllUsersH() {

        List<UserObject> userObjectList = sessionFactory.openSession()
                .createQuery("FROM UserObject ").list();
        return userObjectList;
    }

    public void saveUser(UserObject userObject) {
        sessionFactory.openSession()
                .save(userObject);
    }

    public boolean usernameAvailableH(String username) {
        boolean available = true;
        List<UserObject> userObjects = sessionFactory.openSession()
                .createQuery("from UserObject where username =: username")
                .setParameter("username", username).list();

        if (userObjects.size() > 0) {
            available = false;
        }
        return available;
    }

    public UserObject getUserByTokenH(String token) {
        UserObject userObject = null;
        List<UserObject> userObjectList = sessionFactory.openSession()
                .createQuery("FROM UserObject WHERE token = :token")
                .setParameter("token", token)
                .list();

        if (userObjectList.size() > 0) {
            userObject = userObjectList.get(0);
        }
        return userObject;
    }

    public void addUser(String username, String token) {
        try {
            PreparedStatement preparedStatement =
                    this.connection
                            .prepareStatement("INSERT INTO users (username, token) VALUE (?,?)");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, token);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void addLiveGame (String team1, String team2,int team1Goals,int team2Goals) {
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("INSERT INTO live_games (team1, team2,team1Goals, team2Goals) VALUE (?,?,?,?)");
            preparedStatement.setString(1, team1);
            preparedStatement.setString(2, team2);
            preparedStatement.setInt(3,team1Goals);
            preparedStatement.setInt(4,team2Goals);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addLiveGameH (Match match) {
        sessionFactory.openSession().save(match);
    }

    public void addTeam1Goals (String team1, int team1Goals){
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("INSERT INTO live_games (team1Goals) VALUE (?) WHERE team1=team1");
            preparedStatement.setInt(1, team1Goals);
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void addTeam2Goals (String team2, int team2Goals){
        try {
            PreparedStatement preparedStatement =
                    this.connection.prepareStatement("INSERT INTO live_games (team2Goals) VALUE (?) WHERE team2=team2");
            preparedStatement.setInt(1, team2Goals);
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public boolean usernameAvailable(String username) {
        boolean available = false;
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(
                    "SELECT id " +
                            "FROM users " +
                            "WHERE username = ?");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                available = false;
            } else {
                available = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return available;
    }

    public UserObject getUserByToken(String token) {
        UserObject user1 = null;
        try {
            PreparedStatement preparedStatement = this.connection
                    .prepareStatement(
                            "SELECT id, username FROM users WHERE token = ?");
            preparedStatement.setString(1, token);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
//
                UserObject user = new UserObject();
                user.setUsername(username);
                user.setToken(token);
                user.setId(id);
                user1 = user;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user1;
    }

    public String getUserByCredsH(String username, String token) {
        String response = null;
        List<UserObject> userObjectList = sessionFactory.openSession()
                .createQuery("from UserObject where username =: username and token = : token")
                .setParameter("username", username).setParameter("token", token).list();
        if (userObjectList.size() > 0) {
            UserObject userObject = userObjectList.get(0);
            response = userObject.getToken();
        }
        return response;
    }

    public List<Match> getLiveMatches() {
        return sessionFactory.openSession().createQuery("from Match ").list();
    }

    /*public String getUserByCreds (String username, String token) {
        String response = null;
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND token = ?");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, token);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                response = token;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return response;
    }*/
}
