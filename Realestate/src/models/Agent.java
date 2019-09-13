package models;

/**
 * @author Chuang Wang
 * @studentID 791793
 * @institution University of Melbourne
 */
public class Agent extends User{
    private AgencyCompany company;
    private String phone;
    private String bio;

    public Agent(int id, String email, String password, String name, String phone, String bio,
                 String company_name, String company_address, String company_website) {
        super(id, email, password, name);
        this.phone = phone;
        this.bio = bio;
        company = new AgencyCompany(company_name, company_address, company_website);
    }

    public AgencyCompany getCompany() {
        return company;
    }

    public void setCompany(AgencyCompany company) {
        this.company = company;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}