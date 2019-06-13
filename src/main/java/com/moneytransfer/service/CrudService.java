package com.moneytransfer.service;


import java.util.Optional;

interface CrudService<T> {
	T save(T obj);
	Optional<T> findById(final Long id);
	void deleteById(final Long id);
}
