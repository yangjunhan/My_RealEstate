package dataSourceLayer.mappers.favoriteList;

//import dbConfig.DBConnection;

import dataSourceLayer.dbConfig.DBConnection;
import models.Property;
import utils.ConstructFavoriteListSQLStmt;
import utils.ConstructObjectFromDB;
import utils.ConstructPropertySQLStmt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chuang Wang
 * @studentID 791793
 * @institution University of Melbourne
 */

/**
 * Favorite List data mapper implementation
 */

public class FavoriteListMapper {
    //---------------------------- singleton pattern setup ---------------------------------------
    private static FavoriteListMapper favoriteListMapper;

    private FavoriteListMapper() {
        //
    }

    public static FavoriteListMapper getInstance() {
        if (favoriteListMapper == null) {
            return new FavoriteListMapper();
        }
        return favoriteListMapper;
    }

    public boolean isPropertyLiked(int client_id, int property_id) {
        // create and initialize an empty list for result
        boolean result = false;
        try {
            // retrieve all the properties' id liked by the client from AST(association table)
            String selectStmtForLike = ConstructFavoriteListSQLStmt.getSelectLikeStmt(client_id, property_id);
            System.out.println(selectStmtForLike);
            PreparedStatement stmtForLike = DBConnection.prepare(selectStmtForLike);
            ResultSet rs = stmtForLike.executeQuery();
            if (rs.next()) {
                result = true;
            }
            // close connections
            rs.close();
            stmtForLike.close();
            DBConnection.close();
        } catch (SQLException e) {
            return result;
        }
        return result;
    }


    /**
     * get a list of properties objects based on client id
     *
     * @param client_id
     * @return A list of properties that the client likes
     */
    public List<Property> getFavoriteProperties(int client_id) {
        // create and initialize an empty list for result
        List<Property> result = new ArrayList<>();
        try {
            // retrieve all the properties' id liked by the client from AST(association table)
            String selectStmtForAST = ConstructFavoriteListSQLStmt.getSelectStmt(client_id);
            PreparedStatement stmtForAST = DBConnection.prepare(selectStmtForAST);
            ResultSet rsAST = stmtForAST.executeQuery();
            while (rsAST.next()) {
                int property_id = rsAST.getInt(2);
                /*
                after retrieving a property id from AST, retrieve all the info of the property by
                its id from PT(property table)
                 */
                String selectStmtForPT = ConstructPropertySQLStmt.getSelectStmt(property_id);
                PreparedStatement stmtForPT = DBConnection.prepare(selectStmtForPT);
                ResultSet rsForPT = stmtForPT.executeQuery();
                // construct a property object and add it to the result list
                while (rsForPT.next()) {
                    /*
                     rsForPT should have only one result each rsAST.next(), because property_id
                     is unique
                     */
                    result.add(ConstructObjectFromDB.constructPropertyByRS(rsForPT));
                }
            }
            // close connections
            rsAST.close();
            stmtForAST.close();
            DBConnection.close();
        } catch (SQLException e) {
            return null;
        }
        return result;
    }

    /**
     * If a property was deleted by its agent, then delete all rows from AST containing the property
     * In other words, make the property unavailable for those who favorite it
     *
     * @param property_id
     * @return true if delete is done, false if exception is thrown
     */
    public boolean deleteRowsByPropertyID(int property_id) {
        try {
            String deleteStatement = ConstructFavoriteListSQLStmt.getDeleteStmtByPID(property_id);
            PreparedStatement stmt = DBConnection.prepare(deleteStatement);
            stmt.executeUpdate();
            // close connections
            stmt.close();
            DBConnection.close();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    /**
     * If a client likes a property, then add a row to this table
     *
     * @param client_id
     * @param property_id
     */
    public void likeAProperty(int client_id, int property_id) throws SQLException {
        String insertStmt = ConstructFavoriteListSQLStmt.getInsertStmt(client_id, property_id);
        PreparedStatement stmt = DBConnection.prepare(insertStmt);
        stmt.executeUpdate();
        // close connections
        stmt.close();
        DBConnection.close();
    }

    /**
     * If a client unlikes a property, the delete a single row from AST
     *
     * @param client_id
     * @param property_id
     */
    public void unlikeProperty(int client_id, int property_id) throws SQLException {
        String deleteStmt = ConstructFavoriteListSQLStmt.getDeleteStmtByPIDCID(client_id, property_id);
        PreparedStatement stmt = DBConnection.prepare(deleteStmt);
        stmt.executeUpdate();
        // close connections
        stmt.close();
        DBConnection.close();
    }
}
