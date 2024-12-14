/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexao.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Item;
/**
 *
 * @author Desktop
 */
public class ItemDAO {
    private Conexao conexao;
    private Connection conn;

    public ItemDAO() {
        this.conexao = new Conexao();
        this.conn = this.conexao.getConexao();
    }

    public void inserir(Item item) {
        String sql = "INSERT INTO Item (nome, categoria, estado) VALUES (?, ?, ?)";
        try {
            PreparedStatement stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, item.getNome());
            stmt.setString(2, item.getCategoria());
            stmt.setString(3, item.getEstado());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println("Erro ao inserir item: " + ex.getMessage());
        }
    }

    public Item getItem(int id) {
        String sql = "SELECT * FROM Item WHERE idItem = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Item i = new Item();
                i.setId(id);
                i.setNome(rs.getString("nome"));
                i.setCategoria(rs.getString("categoria"));
                i.setEstado(rs.getString("estado"));
                return i;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao consultar item: " + ex.getMessage());
            return null;
        }
    }

    public void editarItem(Item item) {
        try {
            String sql = "UPDATE Item SET nome=?, categoria = ?, estado = ? WHERE idItem = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, item.getNome());
            stmt.setString(2, item.getCategoria());
            stmt.setString(3, item.getEstado());
            stmt.setInt(4, item.getId());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println("Erro ao atualizar item: " + ex.getMessage());
        }
    }

    public void excluir(int id) {
        try {
            String sql = "DELETE FROM Item WHERE idItem = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println("Erro ao excluir item: " + ex.getMessage());
        }
    }
    
    public List<Item> getTodosItens() {
        String sql = "SELECT * FROM Item";
        List<Item> listaItens = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Item i = new Item();
                i.setId(rs.getInt("idItem"));
                i.setNome(rs.getString("nome"));
                i.setCategoria(rs.getString("categoria"));
                i.setEstado(rs.getString("estado"));
                listaItens.add(i);
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao consultar itens: " + ex.getMessage());
        }
        return listaItens;
    }
    
    /**
     * Recupera a lista de itens com o total de empréstimos, combinando dados das tabelas `Emprestimo` e `Historico`.
    * A consulta SQL utiliza um `UNION ALL` para unir os itens dos empréstimos ativos e históricos, e conta o total de empréstimos por item.
    * 
    * O método executa a consulta no banco de dados e popula uma lista de objetos `Item` com os resultados.
    * 
     * @param nome
     * @param categoria
    * @return Lista de objetos `Item` contendo o id, nome, categoria e o total de empréstimos de cada item.
    */
    public List<Item> getItensComEmprestimoPorFiltro(String nome, String categoria) {
        // Consulta SQL para recuperar os itens com o total de empréstimos
        String sql = """
            SELECT 
                emprestimos_combinados.Item_idItem AS id_Item,
                i.nome,
                i.categoria,
                COUNT(*) AS total_emprestimos
            FROM (
                SELECT Item_idItem FROM Emprestimo WHERE dataDevolucao IS NULL
                UNION ALL
                SELECT Item_idItem FROM Historico
            ) AS emprestimos_combinados
            JOIN Item AS i ON i.idItem = emprestimos_combinados.Item_idItem
                     WHERE i.nome LIKE ? AND i.categoria LIKE ?
            GROUP BY i.idItem, i.nome, i.categoria
            ORDER BY total_emprestimos DESC;
        """;
    
        List<Item> listaItensComEmprestimo = new ArrayList<>();
    
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            String nomeU = "%"+nome+"%";
            stmt.setString(1, nomeU);
            String categoriaU = "%"+categoria+"%";
            stmt.setString(2, categoriaU);
            
            ResultSet rs = stmt.executeQuery();
            
            // Itera sobre os resultados da consulta e preenche a lista de itens
            while (rs.next()) {
                Item i = new Item();
                i.setId(rs.getInt("id_Item"));                // ID do item
                i.setNome(rs.getString("nome"));              // Nome do item
                i.setCategoria(rs.getString("categoria"));    // Categoria do item
                i.setTotalEmprestimos(rs.getString("total_emprestimos")); // Total de empréstimos
                listaItensComEmprestimo.add(i);               // Adiciona o item à lista
            }
        } catch (SQLException ex) {
            // Caso ocorra algum erro durante a execução da consulta
            System.out.println("Erro ao consultar itens emprestados: " + ex.getMessage());
        }
    
        // Retorna a lista de itens com seus respectivos totais de empréstimos
        return listaItensComEmprestimo;
    }
    
    public boolean isItemCadastrada(String nome) {
        String sql = "SELECT 1 FROM Item WHERE nome = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nome);
            ResultSet rs = stmt.executeQuery();
            // Se houver resultado, significa que o nome já está cadastrada
            return rs.next();
        } catch (SQLException ex) {
            System.out.println("Erro ao verificar nome: " + ex.getMessage());
            return false; // Retorna false em caso de erro
        }
    }
    
    public List<Item> getEmprestados() {
        String sql = "SELECT * FROM Item WHERE estado = 'emprestado'";
        List<Item> listaItens = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Item i = new Item();
                i.setId(rs.getInt("idItem"));
                i.setNome(rs.getString("nome"));
                i.setCategoria(rs.getString("categoria"));
                i.setEstado(rs.getString("estado"));
                listaItens.add(i);
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao consultar itens: " + ex.getMessage());
        }
        return listaItens;
    }
    
    public List<Item> getDisponiveis() {
        String sql = "SELECT * FROM Item WHERE estado = 'disponivel'";
        List<Item> listaItens = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Item i = new Item();
                i.setId(rs.getInt("idItem"));
                i.setNome(rs.getString("nome"));
                i.setCategoria(rs.getString("categoria"));
                i.setEstado(rs.getString("estado"));
                listaItens.add(i);
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao consultar itens: " + ex.getMessage());
        }
        return listaItens;
    }
    
    public Integer buscarIdItemPorNome(String nome) {
        String sql = "SELECT idItem FROM Item WHERE nome = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nome);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("idItem");
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao buscar ID do item por nome: " + ex.getMessage());
        }
        return null;
    }

    public Item buscarItemPorNome(String nome) {
        String sql = "SELECT * FROM Item WHERE nome = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nome);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Item item = new Item();
                item.setId(rs.getInt("idItem"));
                item.setNome(rs.getString("nome"));
                // Adicione outros atributos conforme necessário
                return item;
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao buscar item por nome: " + ex.getMessage());
        }
        return null;
    }
}
