package vn.iotstar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import vn.iotstar.models.LoginResponse;
import vn.iotstar.models.LoginUserModel;
import vn.iotstar.models.RegisterUserModel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JwtIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testFullAuthFlowWithNimbusJwt() throws Exception {
        // 1. Đăng ký tài khoản mới (/auth/signup)
        RegisterUserModel registerUser = new RegisterUserModel("trungnh@hcmute.edu.vn", "123456", "ThS. Nguyen Huu Trung");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("trungnh@hcmute.edu.vn"))
                .andExpect(jsonPath("$.fullName").value("ThS. Nguyen Huu Trung"));

        // 2. Đăng nhập để nhận Nimbus JWT (/auth/login)
        LoginUserModel loginUser = new LoginUserModel("trungnh@hcmute.edu.vn", "123456");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
        String jwtToken = loginResponse.getToken();
        assertNotNull(jwtToken);

        // 3. Truy cập endpoint bảo vệ (/users/me) không kèm Token -> phải bị 403 Forbidden
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());

        // 4. Truy cập endpoint bảo vệ (/users/me) với Nimbus Bearer Token -> 200 OK
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("trungnh@hcmute.edu.vn"))
                .andExpect(jsonPath("$.fullName").value("ThS. Nguyen Huu Trung"));

        // 5. Truy cập danh sách users (/users) với Nimbus Bearer Token -> 200 OK
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("trungnh@hcmute.edu.vn"));
    }
}
