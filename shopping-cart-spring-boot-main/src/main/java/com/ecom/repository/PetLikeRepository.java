package com.ecom.repository;

import com.ecom.model.Pet;
import com.ecom.model.PetLike;
import com.ecom.model.UserDtls;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetLikeRepository extends JpaRepository<PetLike, Long> {

	// เช็กว่า user คนนี้เคยกด like ให้ pet นี้หรือยัง
	boolean existsByUserAndPet(UserDtls user, Pet pet);

	// ดึง like object ที่ user กดให้กับ pet (ถ้ามี)
	PetLike findByUserAndPet(UserDtls user, Pet pet);

	// นับจำนวน like ทั้งหมดของ pet นี้
	long countByPet(Pet pet);

	// ลบ like ทั้งหมดของ pet นี้ (ใช้เวลาลบสัตว์เลี้ยง)
	void deleteByPet(Pet pet);
}
