package com.nexusbank.auth.config;

import com.nexusbank.auth.controller.AdminTestController;
import com.nexusbank.auth.controller.CustomerTestController;
import com.nexusbank.auth.security.JwtAuthenticationFilter;
import com.nexusbank.auth.security.JwtService;
import com.nexusbank.auth.security.NexusBankUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest({
                AdminTestController.class,
                CustomerTestController.class
})
@Import({
                SecurityConfig.class,
                JwtAuthenticationFilter.class
})
class SecurityConfigTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private NexusBankUserDetailsService userDetailsService;

        @Test
        void protectedEndpointWithoutAuthenticationShouldReturn401()
                        throws Exception {

                mockMvc.perform(
                                get("/api/customer/test"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error").value("Unauthorized"))
                                .andExpect(jsonPath("$.message").value("Authentication required"))
                                .andExpect(jsonPath("$.path").value("/api/customer/test"));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void customerShouldAccessCustomerEndpoint()
                        throws Exception {

                mockMvc.perform(
                                get("/api/customer/test")).andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void customerShouldNotAccessAdminEndpoint()
                        throws Exception {

                mockMvc.perform(
                                get("/api/admin/test"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.status").value(403))
                                .andExpect(jsonPath("$.error").value("Forbidden"))
                                .andExpect(jsonPath("$.message").value("Access denied"))
                                .andExpect(jsonPath("$.path").value("/api/admin/test"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void adminShouldAccessAdminEndpoint()
                        throws Exception {

                mockMvc.perform(
                                get("/api/admin/test")).andExpect(status().isOk());
        }
}