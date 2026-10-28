import java.util.HashMap;
import java.util.Scanner;


// PARTE 1: interface, classe abstrata e filhas

interface Descontavel {
    void aplicarDesconto(double porcentagem);
}

abstract class Produto implements Descontavel {
    private String nome;
    private double preco;
    private int codigo;
    private int quantidade;

    public Produto(String nome, double preco, int codigo, int quantidade) {
        this.nome = nome;
        this.preco = preco;
        this.codigo = codigo;
        this.quantidade = quantidade;
    }

    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    public int getCodigo() { return codigo; }
    public int getQuantidade() { return quantidade; }

    @Override
    public void aplicarDesconto(double porcentagem) {
        this.preco = this.preco - (this.preco * porcentagem / 100);
    }

    public abstract String getCategoria();

    public double getValorPorQuantidade() {
        return this.preco * this.quantidade;
    }

    public static boolean precoValido(double valor) {
        return valor > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Produto outro = (Produto) obj;
        return codigo == outro.codigo;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(codigo);
    }

    @Override
    public String toString() {
        return String.format("%d - %s - R$ %.2f - qtd: %d - %s",
                codigo, nome, preco, quantidade, getCategoria());
    }
}

class ProdutoComum extends Produto {
    public ProdutoComum(String nome, double preco, int codigo, int quantidade) {
        super(nome, preco, codigo, quantidade);
    }

    @Override
    public String getCategoria() {
        return "Comum";
    }
}

class Perecivel extends Produto {
    private String dataValidade;

    public Perecivel(String nome, double preco, int codigo, int quantidade, String dataValidade) {
        super(nome, preco, codigo, quantidade);
        this.dataValidade = dataValidade;
    }

    @Override
    public String getCategoria() {
        return "Perecivel";
    }

    @Override
    public String toString() {
        return super.toString() + " - validade: " + dataValidade;
    }
}

class ProdutoNaoEncontradoException extends Exception {
    public ProdutoNaoEncontradoException(int codigo) {
        super("Produto com código " + codigo + " não encontrado.");
    }
}

// PARTE 3: o estoque (guarda os produtos em um HashMap)

class Estoque {
    // chave = código do produto, valor = o próprio produto
    private HashMap<Integer, Produto> produtos = new HashMap<>();

    public boolean adicionar(Produto p) {
        if (produtos.containsKey(p.getCodigo())) {
            return false;
        }
        produtos.put(p.getCodigo(), p);
        return true;
    }

    public Produto buscar(int codigo) throws ProdutoNaoEncontradoException {
        Produto p = produtos.get(codigo);       // devolve null se a chave não existir
        if (p == null) {
            throw new ProdutoNaoEncontradoException(codigo);
        }
        return p;
    }

    public void remover(int codigo) throws ProdutoNaoEncontradoException {
        if (!produtos.containsKey(codigo)) {
            throw new ProdutoNaoEncontradoException(codigo);
        }
        produtos.remove(codigo);
    }

    public void listar() {
        if (produtos.isEmpty()) {
            System.out.println("Estoque vazio.");
            return;
        }
        for (Produto p : produtos.values()) {
            System.out.println(p);              // println chama o toString de cada um
        }
    }

    public double getValorTotal() {
        double total = 0;
        for (Produto p : produtos.values()) {
            total += p.getValorPorQuantidade();
        }
        return total;
    }
}

// PARTE 4: o menu no console

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Estoque estoque = new Estoque();
        carregarDadosIniciais(estoque);

        int opcao;
        do {
            mostrarMenu();
            opcao = lerInt(sc, "Escolha uma opção: ");

            try {
                switch (opcao) {
                    case 1 -> cadastrar(sc, estoque, false);
                    case 2 -> cadastrar(sc, estoque, true);
                    case 3 -> estoque.listar();
                    case 4 -> buscarProduto(sc, estoque);
                    case 5 -> aplicarDescontoProduto(sc, estoque);
                    case 6 -> removerProduto(sc, estoque);
                    case 7 -> System.out.printf("Valor total em estoque: R$ %.2f%n", estoque.getValorTotal());
                    case 0 -> System.out.println("Encerrando o sistema...");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (ProdutoNaoEncontradoException e) {
                System.out.println("Erro: " + e.getMessage());
            }
            System.out.println();
        } while (opcao != 0);

        sc.close();
    }

    private static void mostrarMenu() {
        System.out.println("===== ESTOQUE =====");
        System.out.println("1 - Cadastrar produto comum");
        System.out.println("2 - Cadastrar produto perecível");
        System.out.println("3 - Listar produtos");
        System.out.println("4 - Buscar produto por código");
        System.out.println("5 - Aplicar desconto");
        System.out.println("6 - Remover produto");
        System.out.println("7 - Valor total do estoque");
        System.out.println("0 - Sair");
    }

    private static void carregarDadosIniciais(Estoque estoque) {
        estoque.adicionar(new ProdutoComum("Mouse", 80.00, 101, 5));
        estoque.adicionar(new ProdutoComum("Teclado", 150.00, 102, 3));
        estoque.adicionar(new Perecivel("Queijo", 10.00, 202, 2, "10/12/2026"));
    }

    private static void cadastrar(Scanner sc, Estoque estoque, boolean perecivel) {
        int codigo = lerInt(sc, "Código: ");
        System.out.print("Nome: ");
        String nome = sc.nextLine();
        double preco = lerDouble(sc, "Preço: ");
        int quantidade = lerInt(sc, "Quantidade: ");

        if (!Produto.precoValido(preco) || quantidade < 0) {
            System.out.println("O preço deve ser maior que zero e a quantidade não pode ser negativa.");
            return;
        }

        Produto novo;                           // pode guardar um ProdutoComum OU um Perecivel
        if (perecivel) {
            System.out.print("Data de validade (dd/mm/aaaa): ");
            novo = new Perecivel(nome, preco, codigo, quantidade, sc.nextLine());
        } else {
            novo = new ProdutoComum(nome, preco, codigo, quantidade);
        }

        if (estoque.adicionar(novo)) {
            System.out.println("Produto cadastrado!");
        } else {
            System.out.println("Já existe um produto com o código " + codigo + ".");
        }
    }

    private static void buscarProduto(Scanner sc, Estoque estoque) throws ProdutoNaoEncontradoException {
        int codigo = lerInt(sc, "Código: ");
        System.out.println(estoque.buscar(codigo));
    }

    private static void aplicarDescontoProduto(Scanner sc, Estoque estoque) throws ProdutoNaoEncontradoException {
        int codigo = lerInt(sc, "Código: ");
        double percentual = lerDouble(sc, "Desconto (%): ");

        if (percentual <= 0 || percentual > 100) {
            System.out.println("O desconto deve ser maior que 0 e no máximo 100.");
            return;
        }

        Produto p = estoque.buscar(codigo);
        p.aplicarDesconto(percentual);
        System.out.println("Desconto aplicado: " + p);
    }

    private static void removerProduto(Scanner sc, Estoque estoque) throws ProdutoNaoEncontradoException {
        int codigo = lerInt(sc, "Código: ");
        estoque.remover(codigo);
        System.out.println("Produto removido.");
    }

    private static int lerInt(Scanner sc, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private static double lerDouble(Scanner sc, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                // aceita vírgula (12,50) trocando por ponto antes de converter
                return Double.parseDouble(sc.nextLine().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido (ex.: 12,50).");
            }
        }
    }
}
