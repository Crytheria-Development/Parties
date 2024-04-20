package com.cjcameron92.crytheria.parties.model;

import lombok.Data;

import java.util.UUID;

@Data
public class PlayerData {

    private final UUID uuid;
    private final String name;
}
