package techbit.snow.proxy.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static techbit.snow.proxy.service.stream.TestingFrames.*;

@ExtendWith( MockitoExtension.class)
class SnowDataFrameTest {

    @Test
    void givenFrame_whenAttachingBasis_thenNewFrameContainingThatBasisIsReturned() {
        SnowDataFrame frame = frameWithNoBasis(1);
        SnowBasis basis = basis(1);

        SnowDataFrame newFrame = frame.withBasis(basis);

        assertNotSame(frame, newFrame);
        assertNotSame(frame.basis(), basis);
        assertSame(newFrame.basis(), basis);
    }

    @Test
    void givenFrameWithBasis_whenAttachingSameBasis_thenSameFrameIsReturned() {
        SnowDataFrame frame = frameWithBasis(1, 1);

        SnowDataFrame newFrame = frame.withBasis(basis(1));

        assertSame(frame, newFrame);
    }

    @Test
    void givenLastFrame_whenAttachingBasis_thenBasisIsIgnoredAndReferenceToLastFrameIsReturned() {
        assertSame(SnowDataFrame.LAST, SnowDataFrame.LAST.withBasis(basis(1)));
    }
}