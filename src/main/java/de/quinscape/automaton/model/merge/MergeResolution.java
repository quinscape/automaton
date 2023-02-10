package de.quinscape.automaton.model.merge;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Encapsulates the result of a merge operation. Used in-memory on the client-side.
 */
public class MergeResolution
{
    private MergeResolutionOperation operation;

    private List<MergeResolutionEntity> resolutions;


    /**
     * Resolutions to apply to the working set.
     * 
     */
    @NotNull
    public List<MergeResolutionEntity> getResolutions()
    {
        return resolutions;
    }


    public void setResolutions(List<MergeResolutionEntity> resolutions)
    {
        this.resolutions = resolutions;
    }


    /**
     * Operation selected by the user.
     */
    public MergeResolutionOperation getOperation()
    {
        return operation;
    }


    public void setOperation(MergeResolutionOperation operation)
    {
        this.operation = operation;
    }
}
