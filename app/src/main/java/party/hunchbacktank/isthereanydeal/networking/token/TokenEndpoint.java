package party.hunchbacktank.isthereanydeal.networking.token;

import party.hunchbacktank.isthereanydeal.model.authentication.Token;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Arran on 14/03/2016.
 */
public interface TokenEndpoint {
    @FormUrlEncoded
    //@POST("/oauth/token")
    @POST("/13we1391")
    Call<Token> getToken(@Field("grant_type") String grantType, @Field("code") String code, @Field("client_id") String clientId, @Field("client_secret") String clientSecret, @Field("redirect_uri") String redirectUri);
}