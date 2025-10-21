package com.sofka.tagoKoder.backend.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sofka.tagoKoder.backend.client.model.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

}
