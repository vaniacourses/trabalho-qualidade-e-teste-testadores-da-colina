package Controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/*
 * Classe auxiliar para testes.
 * O servlet cadastro.java lê o JSON enviado no POST usando request.getInputStream().
 * Em um servidor real, o Tomcat fornece esse InputStream automaticamente.
 * No teste unitário não existe requisição real, então criamos um InputStream falso
 * contendo o JSON que queremos simular.
 */
public class ServletInputStreamFake extends ServletInputStream {

    private final ByteArrayInputStream inputStream;

    public ServletInputStreamFake(String conteudo) {
        this.inputStream = new ByteArrayInputStream(conteudo.getBytes());
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public boolean isFinished() {
        return inputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        // Não é necessário neste teste
    }
}
