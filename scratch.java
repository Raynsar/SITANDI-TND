import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

public class TestTnde {
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://192.168.1.131:1323/api/login";
        System.out.println("URL: " + url);
        System.out.println("Method: POST");
        
        Map<String, String> body = new HashMap<>();
        body.put("username", "staftes");
        body.put("password", "j4t1mprov");
        
        System.out.println("Request Body: " + body);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("HTTP Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
        } catch (Exception e) {
            System.out.println("HTTP Status: N/A (Exception thrown)");
            System.out.println("Response Body: N/A");
            System.out.println("Exception: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
