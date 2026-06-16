package Controllers;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import DAO.DaoCliente;
import Model.Cliente;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class CadastroTest {

    private String jsonCadastroValido() {
        return "{"
                + "\"usuario\":{"
                + "\"nome\":\"Cadu\","
                + "\"sobrenome\":\"Alves\","
                + "\"telefone\":\"21999999999\","
                + "\"usuario\":\"cadu\","
                + "\"senha\":\"123\""
                + "},"
                + "\"endereco\":{"
                + "\"bairro\":\"Centro\","
                + "\"cidade\":\"Niteroi\","
                + "\"estado\":\"RJ\","
                + "\"complemento\":\"Apto 1\","
                + "\"rua\":\"Rua A\","
                + "\"numero\":10"
                + "}"
                + "}";
    }

    @Test
    public void deveCadastrarUsuarioComSucesso() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        DaoCliente daoMock = Mockito.mock(DaoCliente.class);

        StringWriter resposta = new StringWriter();
        PrintWriter writer = new PrintWriter(resposta);

        when(request.getInputStream()).thenReturn(new ServletInputStreamFake(jsonCadastroValido()));
        when(response.getWriter()).thenReturn(writer);

        cadastro servlet = new cadastro() {
            @Override
            protected DaoCliente criarDaoCliente() {
                return daoMock;
            }
        };

        servlet.doPost(request, response);

        verify(daoMock).salvar(any(Cliente.class));

        writer.flush();
        assertTrue(resposta.toString().contains("Usuário Cadastrado!"));
    }

    @Test
    public void deveMontarClienteComDadosDoJson() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        DaoCliente daoMock = Mockito.mock(DaoCliente.class);

        StringWriter resposta = new StringWriter();
        PrintWriter writer = new PrintWriter(resposta);

        when(request.getInputStream()).thenReturn(new ServletInputStreamFake(jsonCadastroValido()));
        when(response.getWriter()).thenReturn(writer);

        cadastro servlet = new cadastro() {
            @Override
            protected DaoCliente criarDaoCliente() {
                return daoMock;
            }
        };

        servlet.doPost(request, response);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(daoMock).salvar(captor.capture());

        Cliente clienteSalvo = captor.getValue();

        assertEquals("Cadu", clienteSalvo.getNome());
        assertEquals("Alves", clienteSalvo.getSobrenome());
        assertEquals("21999999999", clienteSalvo.getTelefone());
        assertEquals("cadu", clienteSalvo.getUsuario());
        assertEquals("123", clienteSalvo.getSenha());
        assertEquals(1, clienteSalvo.getFg_ativo());

        assertNotNull(clienteSalvo.getEndereco());
        assertEquals("Centro", clienteSalvo.getEndereco().getBairro());
        assertEquals("Niteroi", clienteSalvo.getEndereco().getCidade());
        assertEquals("RJ", clienteSalvo.getEndereco().getEstado());
        assertEquals("Apto 1", clienteSalvo.getEndereco().getComplemento());
        assertEquals("Rua A", clienteSalvo.getEndereco().getRua());
        assertEquals(10, clienteSalvo.getEndereco().getNumero());
    }
}