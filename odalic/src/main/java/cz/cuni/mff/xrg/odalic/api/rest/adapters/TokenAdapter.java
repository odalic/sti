package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.TokenValue;
import cz.cuni.mff.xrg.odalic.users.Token;


public final class TokenAdapter extends XmlAdapter<TokenValue, Token> {

  @Override
  public TokenValue marshal(Token bound) throws Exception {
    return new TokenValue(bound);
  }

  @Override
  public Token unmarshal(TokenValue value) throws Exception {
    return new Token(value.getToken());
  }
}
