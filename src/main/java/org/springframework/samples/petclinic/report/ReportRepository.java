@Repository
public class ReportRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<?> findOwners(String lastName) {

        String sql = "SELECT * FROM owners WHERE last_name='"
                + lastName + "'";

        return entityManager
                .createNativeQuery(sql)
                .getResultList();
    }
}