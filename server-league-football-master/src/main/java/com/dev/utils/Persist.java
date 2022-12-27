
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

//            finishMatch("Arayot-Rahat");
//            List<Match> list = getMatchesFinished();
//            System.out.println();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<Match> getAllMatches() {
        return sessionFactory.openSession().createQuery("from Match")
                .list();
    }



    public List<Match> getMatchesFinished() {
        return  sessionFactory.openSession().createQuery("from Match where isLive =: false")
                .setParameter("false", false).list();
    }
    public boolean finishMatch(String team1) {
        int matchId = (Integer) sessionFactory.openSession().createQuery("select id from Match where team1 = : team")
                .setParameter("team", team1).list().get(0);
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        Match match =  session.get(Match.class, matchId);
        match.setLive(false);
        session.update(match);
        tx.commit();
        return match.isLive();
    }

    public void updateTeam1Goals(String team , int updateGoals) {

        int matchId = (Integer) sessionFactory.openSession().createQuery("select id from Match where team1 = : team")
                .setParameter("team", team).list().get(0);
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
    public void saveUser(UserObject userObject) {
        sessionFactory.openSession()
                .save(userObject);
    }
    public void addLiveGameH (Match match) {
        sessionFactory.openSession().save(match);
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
        return sessionFactory.openSession().createQuery("from Match where isLive =:true")
                .setParameter("true", true)
                .list();
    }






    public void deleteUser(String usernameToDelete) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        session.createQuery("delete from UserObject where username=:username")
                .setParameter("username", usernameToDelete).executeUpdate();
        tx.commit();
        session.close();
    }
}
