package service;

import dataSourceLayer.mappers.addressMapper.AddressMapper;
import dataSourceLayer.mappers.favoriteList.FavoriteListMapper;
import dataSourceLayer.mappers.propertyMapper.PropertyMapper;
import dataSourceLayer.unitOfWork.UnitOfWork;
import models.Address;
import models.Property;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Chuang Wang
 * @studentID 791793
 * @institution University of Melbourne
 */

/**
 * contains core functionality of managing properties published by agents
 */
public class PropertyManagement {
    private static AddressMapper addressMapper = AddressMapper.getInstance();
    private static PropertyMapper propertyMapper = PropertyMapper.getInstance();
    private static FavoriteListMapper favoriteListMapper = FavoriteListMapper.getInstance();

    /**
     * agents publish new properties
     */
    public static void publishProperty(Property property, Address address) throws SQLException {
        UnitOfWork.newCurrent();
        // create an address object
//        Address address = new Address(street, city, state, postal_code, country);

        // store the address to db and get the unique id of the address
        int address_id = addressMapper.createAndGetID(address);

        // store the property into db containing the address id
        property.setAddress_id(address_id);
        //TODO: junhan
//        Property property = new Property(type, Integer.parseInt(num_bed), Integer.parseInt(num_bath),
//                Integer.parseInt(num_carpark), Date.valueOf(date_available), Date.valueOf(date_inspection), description,
//                address_id, rent_or_buy, Integer.parseInt(price), Integer.parseInt(agent_id));

        // use unit of work to insert a property to db
        UnitOfWork.getCurrent().registerNew(property);
        UnitOfWork.getCurrent().commit();
    }

    /**
     * to retrieve a list of properties published by a specific agent
     * @param agentID
     * @return a list of properties objects
     */
    public static List<Property> viewMyPropertyList(int agentID){
//        System.out.println(propertyMapper.searchByAgentID(agentID).size());
        return propertyMapper.searchByAgentID(agentID);
    }

    /**
     * to retrieve a specific property
     * @param property_id
     * @return a property object
     */
    public static Property viewSpecificProperty(int property_id) {
        return propertyMapper.searchByPropertyID(property_id);
    }


    /**
     * to update a property's details
     * @return updated property object
     */
    public static void updateProperty(Property new_property) throws SQLException {
//        old_property.setType(type);
//        old_property.setNum_bed(Integer.parseInt(num_bed));
//        old_property.setNum_bath(Integer.parseInt(num_bath));
//        old_property.setNum_carpark(Integer.parseInt(num_carpark));
//        old_property.setDate_available(Date.valueOf(date_available));
//        old_property.setDate_inspection(Date.valueOf(date_inspection));
//        old_property.setDescription(description);
//        old_property.retrieveTheAddressObj().setStreet(street);
//        old_property.retrieveTheAddressObj().setCity(city);
//        old_property.retrieveTheAddressObj().setState(state);
//        old_property.retrieveTheAddressObj().setPostal_code(postal_code);
//        old_property.retrieveTheAddressObj().setCountry(country);
//        old_property.setRent_or_buy(rent_or_buy);
//        old_property.setPrice(Integer.parseInt(price));
        // update the property in db
        UnitOfWork.newCurrent();
        UnitOfWork.getCurrent().registerDirty(new_property);
        UnitOfWork.getCurrent().commit();;
    }


    /**
     * to delete a published property
     * @param property_id
     * @throws SQLException
     */
    public static void deleteProperty(int property_id) throws SQLException {
        Property property = propertyMapper.searchByPropertyID(property_id);
        UnitOfWork.newCurrent();
        // 1. delete property from association table(favorite list) - AST stands for association
        // table
        favoriteListMapper.deleteRowsByPropertyID(property_id);

        // 2. delete property from property table - PT stands for property table
        UnitOfWork.getCurrent().registerDeleted(property);

        // 3. delete from address table
        UnitOfWork.getCurrent().registerDeleted(property.retrieveTheAddressObj());

        UnitOfWork.getCurrent().commit();
    }
}