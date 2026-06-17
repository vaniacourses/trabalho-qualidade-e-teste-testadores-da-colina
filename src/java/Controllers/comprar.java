    /*
    * To change this license header, choose License Headers in Project Properties.
    * To change this template file, choose Tools | Templates
    * and open the template in the editor.
    */
    package Controllers;

    import DAO.DaoBebida;
    import DAO.DaoCliente;
    import DAO.DaoLanche;
    import DAO.DaoPedido;
    import Helpers.ValidadorCookie;
    import Model.Bebida;
    import Model.Cliente;
    import Model.Lanche;
    import Model.Pedido;
    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.io.PrintWriter;
    import static java.nio.charset.StandardCharsets.ISO_8859_1;
    import static java.nio.charset.StandardCharsets.UTF_8;
    import java.sql.Date;
    import java.text.DateFormat;
    import java.text.SimpleDateFormat;
    import java.time.Instant;
    import java.util.ArrayList;
    import java.util.Iterator;
    import java.util.List;
    import java.util.logging.Level;
    import java.util.logging.Logger;
    import javax.servlet.ServletException;
    import javax.servlet.http.Cookie;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import org.json.JSONObject;

    /**
     *
     * @author kener_000
     */
    public class comprar extends HttpServlet {

        /**
         * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
         * methods.
         *
         * @param request  servlet request
         * @param response servlet response
         * @throws ServletException if a servlet-specific error occurs
         * @throws IOException      if an I/O error occurs
         */
        protected void processRequest(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String json = "";

            //////// Validar Cookie
            boolean resultado = false;

            try {
                Cookie[] cookies = request.getCookies();
                ValidadorCookie validar = criarValidadorCookie();

                resultado = validar.validar(cookies);
            } catch (java.lang.NullPointerException e) {
            }
            //////////////

            if ((br != null) && resultado) {
                json = br.readLine();
                byte[] bytes = json.getBytes(ISO_8859_1);
                String jsonStr = new String(bytes, UTF_8);
                JSONObject dados = new JSONObject(jsonStr);

                DaoCliente clienteDao = criarDaoCliente();

                Cliente cliente = clienteDao.pesquisaPorID(String.valueOf(dados.getInt("id")));

                Iterator<String> keys = dados.keys();

                Double valor_total = 0.00;

                List<Lanche> lanches = new ArrayList<Lanche>();
                List<Bebida> bebidas = new ArrayList<Bebida>();

                while (keys.hasNext()) {

                    String nome = keys.next();
                    if (!nome.equals("id")) {
                        if (dados.getJSONArray(nome).get(1).equals("lanche")) {
                            DaoLanche lancheDao = criarDaoLanche();
                            Lanche lanche = lancheDao.pesquisaPorNome(nome);
                            int quantidade = dados.getJSONArray(nome).getInt(2);
                            lanche.setQuantidade(quantidade);
                            valor_total += lanche.getValor_venda();
                            lanches.add(lanche);
                        }
                        if (dados.getJSONArray(nome).get(1).equals("bebida")) {
                            DaoBebida bebidaDao = criarDaoBebida();
                            Bebida bebida = bebidaDao.pesquisaPorNome(nome);
                            int quantidade = dados.getJSONArray(nome).getInt(2);
                            bebida.setQuantidade(quantidade);
                            valor_total += bebida.getValor_venda();
                            bebidas.add(bebida);
                        }
                    }
                }

                DaoPedido pedidoDao = criarDaoPedido();
                Pedido pedido = new Pedido();
                pedido.setData_pedido(Instant.now().toString());
                pedido.setCliente(cliente);
                pedido.setValor_total(valor_total);
                pedidoDao.salvar(pedido);
                pedido = pedidoDao.pesquisaPorData(pedido);
                pedido.setCliente(cliente);

                System.out.println(lanches.toString());
                for (int i = 0; i < lanches.size(); i++) {
                    pedidoDao.vincularLanche(pedido, lanches.get(i));
                }
                for (int i = 0; i < bebidas.size(); i++) {
                    pedidoDao.vincularBebida(pedido, bebidas.get(i));
                }

                try (PrintWriter out = response.getWriter()) {
                    out.println("Pedido Salvo com Sucesso!");
                }
            } else {
                try (PrintWriter out = response.getWriter()) {
                    out.println("erro");
                }
            }

        }

        protected ValidadorCookie criarValidadorCookie() {
            return new ValidadorCookie();
        }

        protected DaoCliente criarDaoCliente() {
            return new DaoCliente();
        }

        protected DaoLanche criarDaoLanche() {
            return new DaoLanche();
        }

        protected DaoBebida criarDaoBebida() {
            return new DaoBebida();
        }

        protected DaoPedido criarDaoPedido() {
            return new DaoPedido();
        }

        // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the
        // + sign on the left to edit the code.">
        /**
         * Handles the HTTP <code>GET</code> method.
         *
         * @param request  servlet request
         * @param response servlet response
         * @throws ServletException if a servlet-specific error occurs
         * @throws IOException      if an I/O error occurs
         */
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) {
            try {
                processRequest(request, response);
            } catch (ServletException | IOException e) {
                Logger.getLogger(comprar.class.getName())
                        .log(Level.SEVERE, "Erro ao processar requisição GET", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        /**
         * Handles the HTTP <code>POST</code> method.
         *
         * @param request  servlet request
         * @param response servlet response
         * @throws ServletException if a servlet-specific error occurs
         * @throws IOException      if an I/O error occurs
         */
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) {
            try {
                processRequest(request, response);
            } catch (ServletException | IOException e) {
                Logger.getLogger(comprar.class.getName())
                        .log(Level.SEVERE, "Erro ao processar requisição POST", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        /**
         * Returns a short description of the servlet.
         *
         * @return a String containing servlet description
         */
        @Override
        public String getServletInfo() {
            return "Short description";
        }// </editor-fold>

    }
