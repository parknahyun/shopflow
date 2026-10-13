package kr.shopflow.common.config;

import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * 스프링 데이터 REST 노출 규칙(T027, ADR-0008). 읽기만 열고 쓰기는 모두 막는다.
 * <ul>
 *   <li>어노테이션을 붙인 리포지토리만 노출한다(application.yml의 detection-strategy=annotated).</li>
 *   <li>항목에는 PUT, PATCH, DELETE를, 목록에는 POST를 열지 않는다. 상태 변경은 동작 컨트롤러만 한다.</li>
 *   <li>내부 식별자는 응답에 싣지 않는다(기본 동작을 유지하고 exposeIdsFor를 쓰지 않는다).</li>
 * </ul>
 */
@Component
public class RepositoryRestConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.getExposureConfiguration()
                .withItemExposure((metadata, methods) -> methods.disable(HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE))
                .withCollectionExposure((metadata, methods) -> methods.disable(HttpMethod.POST, HttpMethod.PUT,
                        HttpMethod.PATCH, HttpMethod.DELETE))
                .withAssociationExposure((metadata, methods) -> methods.disable(HttpMethod.PUT, HttpMethod.POST,
                        HttpMethod.PATCH, HttpMethod.DELETE));
    }
}
