package zw.codinho.ridehail.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.codinho.ridehail.chat.rest.CreateRideChatMessageRequest;
import zw.codinho.ridehail.chat.rest.RideChatMessageResponse;
import zw.codinho.ridehail.security.AuthRoles;
import zw.codinho.ridehail.shared.api.ApiResponse;
import zw.codinho.ridehail.shared.api.ApiResponseFactory;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rides/{rideId}/chat")
@Tag(name = "Ride Chat", description = "Ride chat between riders and drivers")
public class RideChatController {

    private final RideChatService rideChatService;

    @PostMapping
    @PreAuthorize("hasAnyRole('" + AuthRoles.RIDER + "', '" + AuthRoles.DRIVER + "', '" + AuthRoles.ADMIN + "', '" + AuthRoles.SUPER_USER + "')")
    @Operation(summary = "Send ride chat message", description = "Sends a chat message for a specific ride")
    public ResponseEntity<ApiResponse<RideChatMessageResponse>> sendMessage(@PathVariable UUID rideId,
                                                                            @Valid @RequestBody CreateRideChatMessageRequest request) {
        return ApiResponseFactory.created("Ride chat message sent successfully", rideChatService.sendMessage(rideId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('" + AuthRoles.RIDER + "', '" + AuthRoles.DRIVER + "', '" + AuthRoles.ADMIN + "', '" + AuthRoles.SUPER_USER + "')")
    @Operation(summary = "List ride chat messages", description = "Returns the chat conversation for a ride")
    public ResponseEntity<ApiResponse<List<RideChatMessageResponse>>> getMessages(@PathVariable UUID rideId) {
        return ApiResponseFactory.ok("Ride chat fetched successfully", rideChatService.getMessages(rideId));
    }
}
