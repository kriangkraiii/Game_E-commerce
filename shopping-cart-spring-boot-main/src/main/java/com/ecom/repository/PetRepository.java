package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Pet;
import com.ecom.model.UserDtls;
	public interface PetRepository extends JpaRepository<Pet, Integer> {
	    List<Pet> findByOwner(UserDtls owner);
	}


