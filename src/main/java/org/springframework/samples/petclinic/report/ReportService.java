@Service
public class ReportService {

    @Autowired
    private ReportRepository repository;

    public List<?> search(String lastName) {
        return repository.findOwners(lastName);
    }
}