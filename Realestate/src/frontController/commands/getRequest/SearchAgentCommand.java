package frontController.commands.getRequest;

import frontController.commands.FrontCommand;
import models.User;
import service.AppSession;
import service.UserManagement;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Get Search Agents Page Command
 */
public class SearchAgentCommand extends FrontCommand {
    public void process() throws ServletException, IOException {
        String name = request.getParameter("name");
        // get user list based on the input name through service layer
        List<User> ul = UserManagement.findAgents(name);
        // set user list in session
        AppSession.setAgentList(ul);
        forward("/agent-results.jsp");
    }
}