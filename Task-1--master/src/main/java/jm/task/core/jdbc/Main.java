package jm.task.core.jdbc;

import jm.task.core.jdbc.service.UserService;
import jm.task.core.jdbc.service.UserServiceImpl;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();
        userService.createUsersTable();
        userService.saveUser("Ira", "Gordionok",(byte) 111);
        System.out.println("User c имненем '" + userService.getAllUsers().get(0).getName() + "' добавлен в базу данных");
        userService.saveUser("Drako", "Malfoi",(byte) 11);
        System.out.println("User c имненем '" + userService.getAllUsers().get(1).getName() + "' добавлен в базу данных");
        userService.saveUser("Hagrid", "Bombini",(byte) 1110);
        System.out.println("User c имненем '" + userService.getAllUsers().get(2).getName() + "' добавлен в базу данных");
        userService.saveUser("Mirtle", "Franco",(byte) 101);
        System.out.println("User c имненем '" + userService.getAllUsers().get(3).getName() + "' добавлен в базу данных");
        userService.getAllUsers().toString();
        userService.cleanUsersTable();
        userService.dropUsersTable();
    }
}
