package com.sofka.tagoKoder.backend.account.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;

import com.sofka.tagoKoder.backend.account.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.integration.client.ClientGateway;
import com.sofka.tagoKoder.backend.account.mapper.AccountMapper;
import com.sofka.tagoKoder.backend.account.model.Account;
import com.sofka.tagoKoder.backend.account.model.dto.AccountDto;
import com.sofka.tagoKoder.backend.account.model.dto.PageResponse;
import com.sofka.tagoKoder.backend.account.model.dto.PartialAccountDto;
import com.sofka.tagoKoder.backend.account.repository.AccountRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;
  private final ClientGateway clientGateway;
  private final AccountMapper accountMapper;
  private final R2dbcEntityTemplate template;

  public AccountServiceImpl(AccountRepository accountRepository,
                            ClientGateway clientGateway,
                            AccountMapper accountMapper,
                            R2dbcEntityTemplate template) {
    this.accountRepository = accountRepository;
    this.clientGateway = clientGateway;
    this.accountMapper = accountMapper;
    this.template = template;
  }

  @Override
  public Mono<PageResponse<AccountDto>> getAll(int page, int size, String sortBy, String direction) {
    int p = Math.max(page, 0);
    int s = size <= 0 ? 10 : size;

    Sort sort = Sort.by(
        "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
        (sortBy == null || sortBy.isBlank()) ? "id" : sortBy
    );

    Query q = Query.empty().sort(sort).limit(s).offset((long) p * s);

    Mono<Long> totalMono = template.count(Query.empty(), Account.class);
    Flux<AccountDto> itemsFlux = template.select(q, Account.class).map(accountMapper::toDto);

    return itemsFlux.collectList()
        .zipWith(totalMono)
        .map(tuple -> {
          var content = tuple.getT1();
          long total = tuple.getT2();
          int totalPages = (int) Math.ceil(total / (double) s);
          boolean isLast = p >= (totalPages - 1);
          return new PageResponse<>(content, p, s, total, totalPages, isLast);
        });
  }

  @Override
  public Flux<AccountDto> getAllByClientId(Long clientId) {
    return accountRepository.findAllByClientId(clientId)
        .map(accountMapper::toDto);
  }

  @Override
  public Mono<AccountDto> getById(Long id) {
    return accountRepository.findById(id)
        .switchIfEmpty(Mono.error(new NotFoundException("Account not found with id: " + id)))
        .map(accountMapper::toDto);
  }

  @Override
  public Mono<AccountDto> create(AccountDto accountDto) {
    // Validar cliente en servicio remoto (reactivo)
    return clientGateway.getById(accountDto.getClientId())
        .switchIfEmpty(Mono.error(new NotFoundException("Client not found: " + accountDto.getClientId())))
        .flatMap(__ -> {
          Account entity = accountMapper.toModel(accountDto);
          return accountRepository.save(entity).map(accountMapper::toDto);
        });
  }

  @Override
  public Mono<AccountDto> update(Long id, AccountDto accountDto) {
    return accountRepository.findById(id)
        .switchIfEmpty(Mono.error(new NotFoundException("Account not found with id: " + id)))
        .flatMap(a -> {
          a.setType(accountDto.getType());
          a.setActive(accountDto.isActive());
          return accountRepository.save(a);
        })
        .map(accountMapper::toDto);
  }

  @Override
  public Mono<AccountDto> partialUpdate(Long id, PartialAccountDto partialAccountDto) {
    return accountRepository.findById(id)
        .switchIfEmpty(Mono.error(new NotFoundException("Account not found with id: " + id)))
        .flatMap(a -> {
          a.setActive(partialAccountDto.isActive());
          return accountRepository.save(a);
        })
        .map(accountMapper::toDto);
  }

  @Override
  public Mono<Void> deleteById(Long id) {
    // Soft delete
    return accountRepository.findById(id)
        .switchIfEmpty(Mono.error(new NotFoundException("Account not found with id: " + id)))
        .flatMap(a -> {
          a.setActive(false);
          return accountRepository.save(a);
        })
        .then();
  }

}
