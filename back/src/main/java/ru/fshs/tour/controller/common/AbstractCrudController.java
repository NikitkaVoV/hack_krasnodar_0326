package ru.fshs.tour.controller.common;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.fshs.tour.domain.common.BaseEntity;

public abstract class AbstractCrudController<E extends BaseEntity, R extends JpaRepository<E, UUID>> {

    protected final R repository;

    protected AbstractCrudController(R repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<E> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public E findById(@PathVariable UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public E create(@RequestBody E entity) {
        entity.setId(null);
        return repository.save(entity);
    }

    @PutMapping("/{id}")
    public E update(@PathVariable UUID id, @RequestBody E entity) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found");
        }
        entity.setId(id);
        return repository.save(entity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found");
        }
        repository.deleteById(id);
    }
}
