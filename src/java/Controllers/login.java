package Controllers;

import DAO.DaoCliente;
import DAO.DaoToken;
import Model.Cliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

public class login extends HttpServlet {

    protected DaoCliente criarDaoCliente() {
        return new DaoCliente();
    }

    protected DaoToken criarDaoToken() {
        return new DaoToken();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String json = "";
        boolean resultado = false;

        if (br != null) {
            json = br.readLine();
            JSONObject dados = new JSONObject(json);

            Cliente cliente = new Cliente();
            cliente.setUsuario(dados.getString("usuario"));
            cliente.setSenha(dados.getString("senha"));

            DaoCliente clienteDAO = criarDaoCliente();
            DaoToken tokenDAO = criarDaoToken();
            resultado = clienteDAO.login(cliente);

            if (resultado == true) {
                Cliente clienteCompleto = clienteDAO.pesquisaPorUsuario(cliente);

                Cookie cookie = new Cookie("token", clienteCompleto.getId_cliente() + "-" + Instant.now().toString());
                tokenDAO.salvar(cookie.getValue());
                cookie.setMaxAge(30 * 60);
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
            }
        }
        try (PrintWriter out = response.getWriter()) {
            if (resultado == true) {
                out.println("../carrinho/carrinho.html");
            } else {
                out.println("erro");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            processRequest(request, response);
        } catch (ServletException | IOException e) {
            Logger.getLogger(login.class.getName())
                    .log(Level.SEVERE, "Erro ao processar requisição GET", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            processRequest(request, response);
        } catch (ServletException | IOException e) {
            Logger.getLogger(login.class.getName())
                    .log(Level.SEVERE, "Erro ao processar requisição POST", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
