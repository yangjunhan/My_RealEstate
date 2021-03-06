package dataSourceLayer.mappers.propertyMapper;

import dataSourceLayer.mappers.LockingMapper;
import dataSourceLayer.dbConfig.DBConnection;
import dataSourceLayer.mappers.DataMapper;
import dataSourceLayer.mappers.addressMapper.AddressMapper;
import models.Address;
import models.Property;
import utils.ConstructObjectFromDB;
import utils.ConstructPropertySQLStmt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//import dbConfig.DBConnection;

/**
 * @author Chuang Wang
 * @studentID 791793
 * @institution University of Melbourne
 */

/**
 * Property data mapper implementation
 */
public class PropertyMapper implements DataMapper {
    //---------------------------- singleton pattern setup ---------------------------------------
    private static LockingMapper instance;
    private static PropertyMapper propertyMapper;

    private PropertyMapper() {
        //
    }

    public static LockingMapper getLockingMapperInstance() {
        if (instance == null) {
            instance = new LockingMapper(getSelfInstance());
        }
        return instance;
    }

    public static PropertyMapper getSelfInstance() {
        if (propertyMapper == null) {
            propertyMapper = new PropertyMapper();
        }
        return propertyMapper;
    }

    //------------------------- create, update, delete(Call by UoW) ------------------------------

    /**
     * Feature A - only agents have the permission to create a property information
     * the permission check is done on the domain logic layer
     *
     * @param o
     */
    @Override
    public void create(Object o) throws SQLException {
        Property property = (Property) o;
        int property_id;
        String insertStatement = ConstructPropertySQLStmt.getInsertStmt(property);
        PreparedStatement stmt = DBConnection.prepare(insertStatement);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        // set pk and add to identity map
        property_id = rs.getInt(1);

        property.setId(property_id);
        System.out.println("------------------------" + property + "-------------------------");
        // insert the property into memory(identity map)
        PropertyIdentityMapUtil.addToPropertyIDMap(property);
        PropertyIdentityMapUtil.addToPropertyAgentMap(property);
        // close connections
        rs.close();
        stmt.close();
        DBConnection.close();
    }

    /**
     * Feature A - only agents have the permission to update a property information
     * the permission check is done on the domain logic layer
     *
     * @param o
     */
    public void update(Object o) throws SQLException {
        Property property = (Property) o;
        String updatePropertyStatement = ConstructPropertySQLStmt.getUpdateStmt(property);
        DataMapper am = AddressMapper.getLockingMapperInstance();
        // update the address first
        am.update(property.retrieveTheAddressObj());
        // update the property in db row
        PreparedStatement stmt = DBConnection.prepare(updatePropertyStatement);
        stmt.executeUpdate();

        // update the property in memory(identity map)
        PropertyIdentityMapUtil.addToPropertyIDMap(property);
        PropertyIdentityMapUtil.addToPropertyAgentMap(property);
        // close connections
        stmt.close();
        DBConnection.close();
    }

    /**
     * Feature A - only agents have the permission to delete a property
     * the permission check is done on the domain logic layer
     *
     * @param o
     * @throws SQLException
     */
    @Override
    public void delete(Object o) throws SQLException {
        Property property = (Property) o;
        int property_id = property.getId();
        // delete from property table - PT stands for property table
        String deleteFromPropertyTable = ConstructPropertySQLStmt.getDeleteStmt(property_id);
        PreparedStatement stmtForPT = DBConnection.prepare(deleteFromPropertyTable);
        stmtForPT.executeUpdate();
        // delete the property from memory(identity map)
        Property p = PropertyIdentityMapUtil.getPropertyByPID(property_id);
        if (p != null) {
            PropertyIdentityMapUtil.deleteFromPropertyIDMap(property_id);
            PropertyIdentityMapUtil.deleteFromPropertyAgentMap(p.getAgent_id(), property_id);
        }
        // close connections
        stmtForPT.close();
        DBConnection.close();
    }

    //------------------- read operations (Called by service layer directly) -------------------

    /**
     * Feature A - return a list of properties that the agent posted before
     * the permission check is done on the domain logic layer
     *
     * @param agentID
     * @return a list of properties that the agent posted before
     */
    public List<Property> searchByAgentID(int agentID) {
        List<Property> result = PropertyIdentityMapUtil.getPropertyByAgentID(agentID);
        try {
            if (result == null) {
                result = new ArrayList<>();
                // get all objects from database
                String selectStatement = "SELECT * FROM properties WHERE fk_agent_id = " + agentID;
                PreparedStatement stmt = DBConnection.prepare(selectStatement);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    Property p = ConstructObjectFromDB.constructPropertyByRS(resultSet);
                    result.add(p);
                    PropertyIdentityMapUtil.addToPropertyAgentMap(p);
                }
                // close connections
                resultSet.close();
                stmt.close();
                DBConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * retrieve properties by adding those filters
     *
     * @param property_type
     * @param minBed
     * @param maxBed
     * @param minPrice
     * @param maxPrice
     * @param postCode
     * @return a list of properties that satisfy those criteria
     */
    public List<Property> searchByAllFilters(String rent_or_buy, String property_type, int minBed, int maxBed,
                                             int minPrice, int maxPrice, int postCode) {
        List<Property> result = new ArrayList<>();
        try {
            String selectStmt = ConstructPropertySQLStmt.getSelectStmt(rent_or_buy, property_type, minBed,
                    maxBed, minPrice, maxPrice, postCode);
            PreparedStatement stmt = DBConnection.prepare(selectStmt);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Property property = ConstructObjectFromDB.constructPropertyByRS(rs);
                int address_id = rs.getInt(13);
                String street = rs.getString(14);
                String city = rs.getString(15);
                String state = rs.getString(16);
                int postal_code = rs.getInt(17);
                String country = rs.getString(18);
                // TODO: depends on whether return a map<property, address> or just List<property>
                Address address = new Address(address_id, street, city, state, postal_code, country);
                result.add(property);
            }
            // close connections
            rs.close();
            stmt.close();
            DBConnection.close();

        } catch (SQLException e) {
            return null;
        }
        return result;
    }


    /**
     * search a property information from identity map or database
     *
     * @param property_id
     * @return a property object
     */
    public Property searchByPropertyID(int property_id) {
        Property result = PropertyIdentityMapUtil.getPropertyByPID(property_id);
        try {
            if (result == null) {
                // get the object from database
                String selectStatement = ConstructPropertySQLStmt.getSelectStmt(property_id);
                PreparedStatement stmt = DBConnection.prepare(selectStatement);

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    result = ConstructObjectFromDB.constructPropertyByRS(rs);
                    // add the object to IdentityMap
                    PropertyIdentityMapUtil.addToPropertyIDMap(result);
                }
                // close connections
                rs.close();
                stmt.close();
                DBConnection.close();
            }
        } catch (SQLException e) {
            return null;
        }
        return result;
    }
}
