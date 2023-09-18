package ua.kiev.prog.case2;

import ua.kiev.prog.shared.Id;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDAO<T> {
    private final Connection conn;
    private final String table;

    public AbstractDAO(Connection conn, String table) {
        this.conn = conn;
        this.table = table;
    }

    public void createTable(Class<T> cls) {
        Field[] fields = cls.getDeclaredFields();
        Field id = getPrimaryKeyField(null, fields);

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ")
                .append(table)
                .append("(");

        sql.append(id.getName())
                .append(" ")
                .append(" INT AUTO_INCREMENT PRIMARY KEY,");

        for (Field f : fields) {
            if (f != id) {
                f.setAccessible(true);

                sql.append(f.getName()).append(" ");

                if (f.getType() == int.class) {
                    sql.append("INT,");
                } else if (f.getType() == String.class) {
                    sql.append("VARCHAR(100),");
                } else
                    throw new RuntimeException("Wrong type");
            }
        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");

        try {
            try (Statement st = conn.createStatement()) {
                st.execute(sql.toString());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void add(T t) {
        try {
            Field[] fields = t.getClass().getDeclaredFields();
            Field id = getPrimaryKeyField(t, fields);

            StringBuilder names = new StringBuilder();
            StringBuilder values = new StringBuilder();

            // insert into t (name,age) values("..",..

            for (Field f : fields) {
                if (f != id) {
                    f.setAccessible(true);

                    names.append(f.getName()).append(',');
                    values.append('"').append(f.get(t)).append("\",");
                }
            }

            names.deleteCharAt(names.length() - 1); // last ','
            values.deleteCharAt(values.length() - 1);

            String sql = "INSERT INTO " + table + "(" + names.toString() +
                    ") VALUES(" + values.toString() + ")";

            int newId;
            try (PreparedStatement prepSt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
                prepSt.executeUpdate();
                ResultSet keys = prepSt.getGeneratedKeys();

                if (keys.next()) {
                    newId = keys.getInt(1);
                }
                else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

            id.set(t, newId);

            // TODO: get ID
            // SELECT - X

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void update(T t) {
        try {
            Field[] fields = t.getClass().getDeclaredFields();
            Field id = getPrimaryKeyField(t, fields);

            StringBuilder sb = new StringBuilder();

            for (Field f : fields) {
                if (f != id) {
                    f.setAccessible(true);

                    sb.append(f.getName())
                            .append(" = ")
                            .append('"')
                            .append(f.get(t))
                            .append('"')
                            .append(',');
                }
            }

            sb.deleteCharAt(sb.length() - 1);

            // update t set name = "aaaa", age = "22" where id = 5
            String sql = "UPDATE " + table + " SET " + sb.toString() + " WHERE " +
                    id.getName() + " = \"" + id.get(t) + "\"";

            try (Statement st = conn.createStatement()) {
                st.execute(sql);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void delete(T t) {
        try {
            Field[] fields = t.getClass().getDeclaredFields();
            Field id = getPrimaryKeyField(t, fields);

            String sql = "DELETE FROM " + table + " WHERE " + id.getName() +
                    " = \"" + id.get(t) + "\"";

            try (Statement st = conn.createStatement()) {
                st.execute(sql);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<T> getAll(Class<T> cls, String... args) throws InstantiationException, IllegalAccessException {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + table);
        String strName = "";
        String strAge = "";
        if (args.length > 0){
            sql.append(" WHERE ");
            Field[] fields = cls.getDeclaredFields();
            Field id = getPrimaryKeyField(cls.newInstance(), fields);
            if (args.length == 1){
                for (Field f : fields){
                    if (!f.equals(id) && f.getType().getTypeName().equals(int.class.getTypeName())){
                        strAge = f.getName();
                        sql.append(strAge + " = " + args[0]);
                    }
                }
            }else if (args.length == 2){
                for (Field f : fields){
                    if (!f.equals(id)){
                        if (f.getType().getTypeName().equals(String.class.getTypeName())){
                            strName = f.getName();
                            sql.append(strName + " = \"" + args[0] + "\" AND ");
                        }
                        else{
                            strAge = f.getName();
                            sql.append(strAge + " = " + args[1] + " AND ");
                        }
                    }
                }
            }else{
                throw new IllegalArgumentException("Wrong parametrs!");
            }
        }
        String sqlStr = sql.toString();
        if (sqlStr.endsWith(" AND ")){
            sqlStr = sqlStr.substring(0,sqlStr.length()-5);
        }
        List<T> res = new ArrayList<>();
        try {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery(sqlStr)) {
                    ResultSetMetaData md = rs.getMetaData();

                    while (rs.next()) {
                        T t = cls.getDeclaredConstructor().newInstance(); //!!!

                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            String columnName = md.getColumnName(i);
                            Field field = cls.getDeclaredField(columnName);
                            field.setAccessible(true);

                            field.set(t, rs.getObject(columnName));
                        }

                        res.add(t);
                    }
                }
            }

            return res;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<T> getSelect(Class<T> cls, String... args) throws InstantiationException, IllegalAccessException {
        List<T> res = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ");
        if (args.length == 0){
            throw new IllegalArgumentException("Wrong parametrs!");
        }
        Field[] fields = cls.getDeclaredFields();
        Field id = getPrimaryKeyField(cls.newInstance(), fields);
        sql.append(id.getName() + ", ");
        for (String arg : args){
            sql.append(arg + ", ");
        }
        sql.deleteCharAt(sql.length()-2);

        sql.append("FROM " + table);
        String sqlStr = sql.toString();


        try {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery(sqlStr)) {
                    ResultSetMetaData md = rs.getMetaData();

                    while (rs.next()) {
                        T t = cls.getDeclaredConstructor().newInstance(); //!!!

                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            String columnName = md.getColumnName(i);
                            Field field = cls.getDeclaredField(columnName);
                            field.setAccessible(true);

                            field.set(t, rs.getObject(columnName));
                        }

                        res.add(t);
                    }
                }
            }

            return res;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Field getPrimaryKeyField(T t, Field[] fields) {
        Field result = null;

        for (Field f : fields) {
            if (f.isAnnotationPresent(Id.class)) {
                result = f;
                result.setAccessible(true);
                break;
            }
        }

        if (result == null)
            throw new RuntimeException("No Id field found");

        return result;
    }
}