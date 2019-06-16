import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(urlPatterns = "/customer")
public class Customer extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/new_pool")
    private DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String telephone_search = req.getParameter("telephone");

        if (telephone_search != null){
            try{
                Connection connection=dataSource.getConnection();
                PreparedStatement pstm=connection.prepareStatement("select * from customer where telephone=?");
                pstm.setObject(1,telephone_search);
                ResultSet rst=pstm.executeQuery();

                if (rst.next()){
                    String name = rst.getString("name");
                    String address = rst.getString("address");

                    try (PrintWriter out = resp.getWriter()) {
                        out.println("{\n" +
                                "  \"telephone\": \"" + telephone_search + "\",\n" +
                                "  \"name\": \"" + name + "\",\n" +
                                "  \"address\": \"" + address + "\"\n" +
                                "}");
                    }
                }else{
                    try (PrintWriter out = resp.getWriter()) {
                        out.println("{}");
                    }
                }

            }catch (Exception ex){
                try (PrintWriter out=resp.getWriter()){
                    out.println("{}");
                }
                ex.printStackTrace();
            }
        }else{
            try (PrintWriter out = resp.getWriter()) {

                resp.setContentType("application/json");

                try {
                    Connection connection = dataSource.getConnection();

                    Statement stm = connection.createStatement();
                    ResultSet rst = stm.executeQuery("SELECT * FROM Customer");

                    JsonArrayBuilder customers = Json.createArrayBuilder();

                    while (rst.next()){
                        String telephone = rst.getString("telephone");
                        String name = rst.getString("name");
                        String address = rst.getString("address");


                        JsonObject customer = Json.createObjectBuilder().add("telephone",telephone)
                                .add("name", name)
                                .add("address", address)
                                .build();
                        customers.add(customer);
                    }

                    out.println(customers.build().toString());

                    connection.close();
                } catch (Exception ex) {
                    resp.sendError(500, ex.getMessage());
                    ex.printStackTrace();
                }

            }
        }
    }
}
