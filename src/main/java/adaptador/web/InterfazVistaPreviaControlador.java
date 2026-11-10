package m3.adaptador.web;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class InterfazVistaPreviaControlador {
    @GetMapping({"/ui/preview","/ui/preview/**"})
    public String live(HttpServletRequest request) {
        return "redirect:"+request.getRequestURI().replaceFirst("/ui/preview","/ui");
    }
}
