package tech.minediamond.vortex.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class GlobalDataStore {
    //存放全局的应用信息数据
    public static ArrayList<ProgramInfo> programInfos = new ArrayList<>();
}
