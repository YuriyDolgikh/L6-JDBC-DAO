package ua.kiev.prog.case2;

import ua.kiev.prog.shared.Client;
import ua.kiev.prog.shared.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException {
        try (Connection conn = ConnectionFactory.getConnection())
        {
            // remove this
            try {
                try (Statement st = conn.createStatement()) {
                    st.execute("DROP TABLE IF EXISTS Clients");
                    //st.execute("CREATE TABLE Clients (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(20) NOT NULL, age INT)");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            ClientDAOImpl2 dao = new ClientDAOImpl2(conn, "Clients");
            dao.createTable(Client.class);

            Client c1 = new Client("test1", 10);
            Client c2 = new Client("test2", 20);
            Client c3 = new Client("test1", 10);
            Client c4 = new Client("test2", 10);
            dao.add(c1);
            dao.add(c2);
            dao.add(c3);
            dao.add(c4);
            System.out.println("Client 'c2' added with id= " + c2.getId());
            System.out.println();

            // int id = c.getId();

            List<Client> list = dao.getAll(Client.class);
            printList(list);
            System.out.println();

            list = dao.getAll(Client.class, "test1","10");
            printList(list);
            System.out.println();

            list = dao.getAll(Client.class, "10");
            printList(list);
            System.out.println();

            list = dao.getSelect(Client.class,  "age");
            printList(list);
            System.out.println();

            list = dao.getSelect(Client.class,  "name");
            printList(list);
            System.out.println();

            list = dao.getSelect(Client.class,  "name", "age");
            printList(list);
            System.out.println();

//            list.get(0).setAge(55);
//            dao.update(list.get(0));

            /*

            List<Client> list = dao.getAll(Client.class, "name", "age");
            List<Client> list = dao.getAll(Client.class, "age");
            for (Client cli : list)
                System.out.println(cli);

             */

//            dao.delete(list.get(0));
        }
    }
    public static void printList(List<Client> list){
        if (list.size() != 0){
            for (Client cli : list)
                System.out.println(cli);
        } else{
            System.out.println("No result!");
        }
    }

}
