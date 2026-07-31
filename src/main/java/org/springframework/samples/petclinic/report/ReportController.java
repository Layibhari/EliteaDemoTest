@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService service;

    @GetMapping("/owners")
    public List<?> search(
            @RequestParam String lastName) {

        return service.search(lastName);
    }
}