package br.com.site.screenmatch.service;

public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe );
}
