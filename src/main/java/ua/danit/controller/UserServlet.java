package ua.danit.controller;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ua.danit.dao.ChatDAOtoDB;
import ua.danit.dao.LikedDAOtoDB;
import ua.danit.dao.UserDAOtoDB;
import ua.danit.model.Chat;
import ua.danit.model.Liked;
import ua.danit.model.User;
import ua.danit.utils.GetLoginFromCookie;
import ua.danit.utils.TemplateConfig;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UserServlet extends HttpServlet {

    private Iterator<User> iterator;
    private LikedDAOtoDB likedDAOtoDB;
    private UserDAOtoDB userDAOtoDB;
    private ChatDAOtoDB chatDAOtoDB;

    public UserServlet(UserDAOtoDB userDAOtoDB, LikedDAOtoDB likedDAOtoDB, ChatDAOtoDB chatDAOtoDB) {
        this.likedDAOtoDB = likedDAOtoDB;
        this.userDAOtoDB = userDAOtoDB;
        this.chatDAOtoDB = chatDAOtoDB;
        List<User> users = userDAOtoDB.getAll();
        iterator = users.listIterator();
    }



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {


        Cookie[] cookies = req.getCookies();
        for (Cookie c : cookies) {
            System.out.println("Name: " + c.getName());
            System.out.println("Value: " + c.getValue());
        }

        String userLogin = new GetLoginFromCookie().getLogin(cookies, "login");

        if (iterator.hasNext()){
            User user = iterator.next();
            if (user.getLogin().equals(userLogin) && iterator.hasNext()){
                user = iterator.next();
            } else if (!iterator.hasNext()){

                resp.sendRedirect("/liked?login=" + userLogin);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("name", user.getName());
            model.put("photo", user.getPhoto());
            model.put("login", user.getLogin());
            model.put("myLogin", userLogin);


            Template template = new TemplateConfig().getConfig("like-page.html");
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            Writer out = resp.getWriter();

            try {

                template.process(model, out);

            } catch (TemplateException e) {
                e.printStackTrace();
            }

        } else {

            resp.sendRedirect("/liked?login=" + userLogin);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String like = req.getParameter("like");


        if (like.equals("yes")) {

            String login = req.getParameter("login");
            String myLogin = req.getParameter("myLogin");

            List<Integer> alreadyLikedIdList = likedDAOtoDB.getAllUserIdByLogin(myLogin);
            User user = userDAOtoDB.get(login);
            Integer userId = user.getId();

            if (!alreadyLikedIdList.contains(userId)){

                Chat chat = new Chat(login, myLogin);
                chatDAOtoDB.put(chat);

                Liked liked = new Liked(user.getId(), chat.getChatId(), myLogin);
                likedDAOtoDB.put(liked);
            }

            doGet(req, resp);

        } else {

            doGet(req, resp);

        }


    }
}
