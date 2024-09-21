package me.dio.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.dio.controller.dto.UserDto;
import me.dio.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/users")
@Tag(name = "Users Controller", description = "RESTful API for managing users.")
public record UserController(UserService userService) {

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all registered users")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "200", description = "Operation successful")
    })
    public ResponseEntity<List<UserDto>> findAll() {
        var users = userService.findAll();
        var usersDto = users.stream().map(UserDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(usersDto);
    }
    
    @GetMapping("/teste")
    @Operation(summary = "Get all commoditysssss", description = "Retrieve a list of all registered userssssss")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "200", description = "Operation successful")
    })
    public ResponseEntity<List<UserDto>> findAlls() {
    	  // URL da API (Exemplo, substitua pela URL correta)https://cepea.esalq.usp.br/br
        String apiUrl = "https://cepea.esalq.usp.br/br";

        // Chama a API e obtém os dados
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .GET()
            .build();

        try {
            // Envia a requisição para a API
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parseia a resposta JSON
                com.google.gson.JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

                // Insere os dados no banco de dados
                insertDataIntoDatabase(jsonArray);

            } else {
                System.out.println("Erro ao chamar a API: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // URL da página do CEPEA que contém as cotações do arroz
        String url = "https://www.cepea.esalq.usp.br/br/indicador/arroz.aspx";

        try {
            // Fazendo a conexão com a página
            Document doc = Jsoup.connect(url).get();
            
            

            // Selecionando o elemento que contém a cotação do arroz (modifique conforme necessário)
            // Isso depende da estrutura HTML da página
            Elements rows = doc.select("#imagenet-indicador1 tbody tr"); // Exemplo de seleção de linhas de uma tabela

            for (Element row : rows) {
                // Selecionando as colunas da tabela
                Elements columns = row.select("td");

                // Extraindo informações (dependerá da estrutura da página)
                String data = columns.get(0).text();
                String estado = columns.get(1).text();
                String valor = columns.get(2).text();
                String valors = columns.get(3).text();

                // Imprimindo as informações
                System.out.println("Data: " + data);
                System.out.println("Estado: " + estado);
                System.out.println("Valor: " + valor);
                System.out.println("Valor: " + valors);
                System.out.println("----------------------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        
        var users = userService.findAll();
        var usersDto = users.stream().map(UserDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(usersDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID", description = "Retrieve a specific user based on its ID")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "200", description = "Operation successful"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        var user = userService.findById(id);
        return ResponseEntity.ok(new UserDto(user));
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user and return the created user's data")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "422", description = "Invalid user data provided")
    })
    public ResponseEntity<UserDto> create(@RequestBody UserDto userDto) {
        var user = userService.create(userDto.toModel());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(new UserDto(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Update the data of an existing user based on its ID")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "422", description = "Invalid user data provided")
    })
    public ResponseEntity<UserDto> update(@PathVariable Long id, @RequestBody UserDto userDto) {
        var user = userService.update(id, userDto.toModel());
        return ResponseEntity.ok(new UserDto(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Delete an existing user based on its ID")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    private static void insertDataIntoDatabase(JsonArray jsonArray) throws SQLException {
    	for (JsonElement element : jsonArray) {
		    JsonObject obj = element.getAsJsonObject();
		    String produto = obj.get("produto").getAsString();
		    double preco = obj.get("preco").getAsDouble();
		    String data = obj.get("data").getAsString();
		    
		   System.out.println("Produto: " + produto + " Preço: " + preco + " Data: " + data);
		}

		System.out.println("Dados inseridos no banco com sucesso!");
    }
}
