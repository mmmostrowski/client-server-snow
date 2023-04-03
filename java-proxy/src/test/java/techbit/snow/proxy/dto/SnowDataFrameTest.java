package techbit.snow.proxy.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith( MockitoExtension.class)
class SnowDataFrameTest {

    @Mock
    private SnowBasis basis;

    @Test
    void givenLastFrame_whenAttachingBasis_thenBasisIsIgnoredAndReferenceToLastFrameIsReturned() {
        assertSame(SnowDataFrame.LAST, SnowDataFrame.LAST.withBasis(basis));
    }

}