package io.github.moshkabortmanstar.data;


import io.github.moshkabortmanstar.data.enums.DataType;
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
