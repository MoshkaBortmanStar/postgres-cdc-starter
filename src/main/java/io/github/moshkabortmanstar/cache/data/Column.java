package io.github.moshkabortmanstar.cache.data;


import io.github.moshkabortmanstar.cache.data.enums.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column {

    private String name;
    private DataType dataType;

}
