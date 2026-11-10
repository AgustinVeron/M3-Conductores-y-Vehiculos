package m3.adaptador.persistencia;

import m3.aplicacion.puerto.UnidadDeTrabajo;
import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * UnidadDeTrabajo implementation using Spring's TransactionTemplate.
 */
@Component
public class SpringTransaccionUnidadDeTrabajo implements UnidadDeTrabajo {

    private final TransactionTemplate transactionTemplate;

    public SpringTransaccionUnidadDeTrabajo(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = Objects.requireNonNull(transactionTemplate, "transactionTemplate cannot be null");
    }

    @Override
    public <T> T executeInTransaction(Supplier<T> action) {
        return transactionTemplate.execute(status -> action.get());
    }

    @Override
    public void executeInTransaction(Runnable action) {
        transactionTemplate.executeWithoutResult(status -> action.run());
    }
}

