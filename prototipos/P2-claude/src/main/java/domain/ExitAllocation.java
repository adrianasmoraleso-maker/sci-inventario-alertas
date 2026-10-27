package domain;

import java.time.LocalDate;

/** Parte de una salida tomada de un lote concreto (resultado de la asignación FEFO). */
public record ExitAllocation(String lotCode, LocalDate dueDate, int quantity) { }
