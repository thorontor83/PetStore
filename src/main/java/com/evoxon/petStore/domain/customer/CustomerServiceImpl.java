package com.evoxon.petStore.domain.customer;

import com.evoxon.petStore.dto.CustomerDto;
import com.evoxon.petStore.persistence.CustomerEntity;
import com.evoxon.petStore.persistence.CustomerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ArrayUtils.toArray;

@Service
public class CustomerServiceImpl implements UserDetailsService, CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<CustomerEntity> optionalCustomerEntity = customerRepository.findByUsername(username);
        if(optionalCustomerEntity.isEmpty()){
            throw new UsernameNotFoundException("User not found");
        }
        else{
            Customer customer = CustomerDto.fromEntityToDomain(optionalCustomerEntity.get());
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(customer.getCustomerRole().name()));
            return new CustomUserDetails(customer.getId(),customer.getUsername(), customer.getPassword(), authorities);
        }
    }

    public Customer getCustomerByName(String username) {
        Optional<CustomerEntity> optionalCustomerEntity = customerRepository.findByUsername(username);
        return optionalCustomerEntity.map(CustomerDto::fromEntityToDomain).orElse(null);


    }

    public Customer createCustomer(Customer customer) {
        if(customer.getUsername()==null || customer.getPassword()==null || customer.getCustomerRole()==null){
            return null;
        }
        CustomerEntity customerEntity = customerRepository.saveAndFlush(CustomerDto.fromDomainToEntity(customer));
        return CustomerDto.fromEntityToDomain(customerEntity);
    }

    public String deleteCustomer(String username) {
        Optional<CustomerEntity> optionalCustomerEntityToDelete = customerRepository.findByUsername(username);
        if (optionalCustomerEntityToDelete.isPresent()){
            customerRepository.delete(optionalCustomerEntityToDelete.get());
            return "Customer with username: " + username + " deleted successfully";
        }
        else{
            return null;
        }

    }

    public Customer modifyCustomer(Customer customer) {
        Optional<CustomerEntity> optionalCustomerEntity = customerRepository.findById(customer.getId());
        if (optionalCustomerEntity.isPresent()){
            return CustomerDto.fromEntityToDomain(customerRepository.saveAndFlush(CustomerDto.fromDomainToEntity(customer)));
        }
        else{
            return null;
        }
    }

    public List<Customer> createWithList(List<Customer> customerList) {
        return customerList
                .stream()
                .map(CustomerDto::fromDomainToEntity)
                .map(customerRepository::saveAndFlush)
                .map(CustomerDto::fromEntityToDomain)
                .collect(Collectors.toList());
    }

    public Customer[] createWithArray(Customer[] customerArray) {
        return Arrays.stream(customerArray)
                .map(CustomerDto::fromDomainToEntity)
                .map(customerRepository::saveAndFlush)
                .map(CustomerDto::fromEntityToDomain)
                .toArray(Customer[]::new);
    }
}

